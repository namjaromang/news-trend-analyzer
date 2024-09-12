package co.news.insight.response;

import java.util.List;
import lombok.Data;

@Data
public class GptApiResponse {

  private String id;
  private String object;
  private int created;
  private String model;
  private List<Choice> choices;
  private Usage usage;

  @Data
  public static class Choice {

    private int index;
    private Message message;
    private String finishReason;
  }

  @Data
  public static class Message {

    private String role;
    private String content;
  }

  @Data
  public static class Usage {

    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
  }
}

