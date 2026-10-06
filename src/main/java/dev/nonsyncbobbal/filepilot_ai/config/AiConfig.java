package dev.nonsyncbobbal.filepilot_ai.config;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder,
                                 ToolCallbackProvider toolCallbackProvider) {
        return chatClientBuilder
                .defaultSystem("""
                        You are an AI assistant with access to the file system.

                        You can:
                        - Read files
                        - Create new files
                        - Write content to files
                        - Update existing files
                        - Search for files and directories
                        - Analyze and summarize file contents

                        When a user asks you to perform a file system operation,
                        use the appropriate available tool to perform the operation.

                        Always verify the result of a file system operation before
                        telling the user that it was completed successfully.

                        Never claim that a file was created, updated, written, or
                        deleted unless the corresponding operation actually succeeded.

                        If you do not have the required tool or permission to perform
                        an operation, clearly tell the user instead of pretending
                        that the operation was completed.
                        """)
                .defaultTools(toolCallbackProvider)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
