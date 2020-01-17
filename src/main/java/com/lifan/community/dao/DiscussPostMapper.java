package com.lifan.community.dao;

import com.lifan.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //我发过的帖子
    //offset每页起始的行号
    //limit 每页最多显示多少行
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    //@param注解用于给参数取别名，
    //如果只有一个参数，并且在<if>里使用，则必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);

}
