package mm.teashop.api;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestControllerAdvice public class ApiExceptionHandler { @ExceptionHandler(Exception.class) ResponseEntity<Map<String,String>> handle(Exception e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()==null?"Request failed":e.getMessage()));} }
