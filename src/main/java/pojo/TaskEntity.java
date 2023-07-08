package pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author CaptainUniverse_
 * @date 2023/7/9
 */
@Data
public class TaskEntity implements Serializable
{
    private Long id;
    private LocalDateTime createTime;
    private String data;
    private int sort;
    private int status;
}
