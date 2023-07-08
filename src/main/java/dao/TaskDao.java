package dao;

import pojo.TaskEntity;

import java.util.List;

/**
 * @author CaptainUniverse_
 * @date 2023/7/9
 */
public interface TaskDao
{
   List<TaskEntity> findAll();
}

