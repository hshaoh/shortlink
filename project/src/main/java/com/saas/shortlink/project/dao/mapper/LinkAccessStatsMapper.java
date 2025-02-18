package com.saas.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.saas.shortlink.project.dao.entity.LinkAccessStats;
import com.saas.shortlink.project.dto.ShortLinkGroupStatsDTO;
import com.saas.shortlink.project.dto.ShortLinkStatsDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 短链接基础访问监控持久层
 * 公众号：马丁玩编程，回复：加群，添加马哥微信（备注：link）获取项目资料
 */
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStats> {

    /**
     * 记录基础访问监控数据
     */
    @Insert("""
            INSERT INTO 
            t_link_access_stats (full_short_url, date, pv, uv, uip, hour, weekday, create_time, update_time, del_flag) 
            VALUES( #{linkAccessStats.fullShortUrl}, #{linkAccessStats.date}, #{linkAccessStats.pv}, #{linkAccessStats.uv}, #{linkAccessStats.uip}, #{linkAccessStats.hour}, #{linkAccessStats.weekday}, NOW(), NOW(), 0) 
            ON DUPLICATE KEY UPDATE pv = pv +  #{linkAccessStats.pv}, uv = uv + #{linkAccessStats.uv}, uip = uip + #{linkAccessStats.uip};
            """)
    void shortLinkStats(@Param("linkAccessStats") LinkAccessStats linkAccessStatsDO);

    /**
     * 根据短链接获取指定日期内基础监控数据
     */
    @Select("""
            SELECT 
                tlas.date, 
                SUM(tlas.pv) AS pv, 
                SUM(tlas.uv) AS uv, 
                SUM(tlas.uip) AS uip 
            FROM 
                t_link tl INNER JOIN 
                t_link_access_stats tlas ON tl.full_short_url = tlas.full_short_url 
            WHERE 
                tlas.full_short_url = #{param.fullShortUrl} 
                AND tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0'
                AND tlas.date BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59') 
            GROUP BY
                tlas.full_short_url, tl.gid, tlas.date;
            """)
    List<LinkAccessStats> listStatsByShortLink(@Param("param") ShortLinkStatsDTO requestParam);

    /**
     * 根据分组获取指定日期内基础监控数据
     */
    @Select("""
            SELECT 
                tlas.date, 
                SUM(tlas.pv) AS pv, 
                SUM(tlas.uv) AS uv, 
                SUM(tlas.uip) AS uip 
            FROM 
                t_link tl INNER JOIN 
                t_link_access_stats tlas ON tl.full_short_url = tlas.full_short_url 
            WHERE 
                tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlas.date BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59') 
            GROUP BY 
                tl.gid, tlas.date;
            """)
    List<LinkAccessStats> listStatsByGroup(@Param("param") ShortLinkGroupStatsDTO requestParam);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     */
    @Select("""
            SELECT 
                tlas.hour, 
                SUM(tlas.pv) AS pv 
            FROM 
                t_link tl INNER JOIN 
                t_link_access_stats tlas ON tl.full_short_url = tlas.full_short_url 
            WHERE 
                tlas.full_short_url = #{param.fullShortUrl} 
                AND tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlas.date BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59') 
            GROUP BY 
                tlas.full_short_url, tl.gid, tlas.hour;
            """)
    List<LinkAccessStats> listHourStatsByShortLink(@Param("param") ShortLinkStatsDTO requestParam);

    /**
     * 根据分组获取指定日期内小时基础监控数据
     */
    @Select("""
            SELECT 
                tlas.hour, 
                SUM(tlas.pv) AS pv 
            FROM 
                t_link tl INNER JOIN 
                t_link_access_stats tlas ON tl.full_short_url = tlas.full_short_url 
            WHERE 
                tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlas.date BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59') 
            GROUP BY 
                tl.gid, tlas.hour;
            """)
    List<LinkAccessStats> listHourStatsByGroup(@Param("param") ShortLinkGroupStatsDTO requestParam);

    /**
     * 根据短链接获取指定日期内每周基础监控数据
     */
    @Select("""
            SELECT 
                tlas.weekday, 
                SUM(tlas.pv) AS pv 
            FROM 
                t_link tl INNER JOIN 
                t_link_access_stats tlas ON tl.full_short_url = tlas.full_short_url 
            WHERE 
                tlas.full_short_url = #{param.fullShortUrl} 
                AND tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlas.date BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59') 
            GROUP BY 
                tlas.full_short_url, tl.gid, tlas.weekday;
            """)
    List<LinkAccessStats> listWeekdayStatsByShortLink(@Param("param") ShortLinkStatsDTO requestParam);

    /**
     * 根据分组获取指定日期内每周基础监控数据
     */
    @Select("""
            SELECT 
                tlas.weekday, 
                SUM(tlas.pv) AS pv 
            FROM 
                t_link tl INNER JOIN 
                t_link_access_stats tlas ON tl.full_short_url = tlas.full_short_url 
            WHERE 
                tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlas.date BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59') 
            GROUP BY 
                tl.gid, tlas.weekday;
            """)
    List<LinkAccessStats> listWeekdayStatsByGroup(@Param("param") ShortLinkGroupStatsDTO requestParam);
}