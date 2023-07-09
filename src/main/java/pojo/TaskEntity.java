package pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author CaptainUniverse_
 * @date 2023/7/9
 */
@Data
public class TaskEntity implements Serializable
{
    private Long id;
    private String createTime;
    private String doneTime;
    private String data;
    private int sort;
    private int status;
}
