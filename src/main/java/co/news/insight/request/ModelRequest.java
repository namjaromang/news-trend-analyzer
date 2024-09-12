package co.news.insight.request;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ModelRequest {

  private final String model;
  private final List<Message> messages;

  @Builder
  private ModelRequest(String model, List<Message> messages) {
    this.model = model;
    this.messages = List.copyOf(messages);
  }
}
