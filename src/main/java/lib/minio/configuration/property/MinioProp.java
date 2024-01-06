package lib.minio.configuration.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

//@Data
//@Component
////@ConfigurationProperties("application.minio")
//@ConfigurationProperties(prefix = "minio")
//public class MinioProp {
//  private String url;
//  private String username;
//  private String password;
//  private String getErrorMessage;
//  private String postErrorMessage;
//}

@Data
@Component
@ConfigurationProperties("application.minio")
public class MinioProp {
  private String url;
  private String username;
  private String password;
  private String getErrorMessage;
  private String postErrorMessage;
}
