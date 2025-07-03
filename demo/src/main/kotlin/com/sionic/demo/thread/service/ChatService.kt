package com.sionic.demo.thread.service

import com.sionic.demo.thread.repository.Chat
import com.sionic.demo.thread.repository.ChatRepository
import com.sionic.demo.thread.repository.Thread
import com.sionic.demo.thread.repository.ThreadRepository
import com.sionic.demo.user.repository.User
import com.sionic.demo.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Service
class ChatService(
    private val userRepository: UserRepository,
    private val chatRepo: ChatRepository,
    private val threadRepo: ThreadRepository,
    private val openAiApi: OpenAiApi,
) {

    fun chat(userId: Long, question: String, isStreaming: Boolean, model: String?): Chat {
        val user = userRepository.findById(userId).orElseThrow()
        val cutoff = ZonedDateTime.now().minusMinutes(30)

        val thread = threadRepo.findLatestActiveThread(userId, cutoff).firstOrNull()
            ?: threadRepo.save(Thread(user = user))

        // contextMessages: 과거 대화 포함
        val messages = mutableListOf<Message>(
            SystemMessage("You are a helpful assistant.")
        )

        thread.chats.forEach {
            messages += UserMessage(it.question)
            messages += AssistantMessage(it.answer)
        }

        // 최신 질문 추가
        messages += UserMessage(question)

        // stop 시퀀스와 온도 설정 (원하면 외부에서 받도록 확장 가능)
        val stopSequences = listOf("User:", "Assistant:")
        val temperature = 0.7

        val chatOptions = ChatOptions.builder()
            .model("gpt-3.5-turbo")
            .temperature(temperature)
            .stopSequences(stopSequences)
            .build()
        val prompt = Prompt(messages, chatOptions)

        // 챗 모델 생성
        val chatModel = OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .build()

        // 챗 모델 호출
        val response = chatModel.call(prompt)

        val answer = response.result.output.text ?: throw IllegalStateException("응답이 비어있습니다.")


        return chatRepo.save(Chat(question = question, answer = answer, thread = thread))
    }

    fun listChats(user: User, threadId: Long, pageable: Pageable): Page<Chat> {
        return chatRepo.findByThreadIdOrderByCreatedAtDesc(threadId, pageable)
    }

    fun deleteThread(user: User, threadId: Long) {
        val thread = threadRepo.findById(threadId).orElseThrow()
        require(thread.user == user) { "삭제 권한 없음" }
        threadRepo.delete(thread)
    }
}