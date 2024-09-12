package co.news.insight.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Message {

  private final String role;
  private final String content;

  @Builder
  private Message(String role, String content) {
    this.role = role;
    this.content = content;
  }
}
