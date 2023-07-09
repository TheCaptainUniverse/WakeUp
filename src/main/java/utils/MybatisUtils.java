package utils;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

//sqlSessionFactory --> sqlSession
public class MybatisUtils
{
    private static SqlSessionFactory sqlSessionFactory;

    static
    {
        try
        {
            //获取sqlSessionFactory对象
            String resource = "mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    //获取SqlSession连接,通过SqlSession就可以进行所有CRUD操作了
    public static SqlSession getSession()
    {
        return sqlSessionFactory.openSession(true);
    }
}

