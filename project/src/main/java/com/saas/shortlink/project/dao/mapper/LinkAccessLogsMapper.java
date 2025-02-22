package com.saas.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.saas.shortlink.project.dao.entity.LinkAccessLogs;
import com.saas.shortlink.project.dao.entity.LinkAccessStats;
import com.saas.shortlink.project.dto.ShortLinkGroupStatsDTO;
import com.saas.shortlink.project.dto.ShortLinkStatsDTO;
import com.saas.shortlink.project.dto.ShortLinkGroupStatsAccessRecordDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 访问日志监控持久层
 */
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogs> {

    /**
     * 根据短链接获取指定日期内高频访问IP数据
     */
    @Select("""
            SELECT 
                tlal.ip, 
                COUNT(tlal.ip) AS count 
            FROM 
                t_link tl INNER JOIN 
                t_link_access_logs tlal ON tl.full_short_url = tlal.full_short_url 
            WHERE 
                tlal.full_short_url = #{param.fullShortUrl} 
                AND tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0'
                AND tlal.create_time BETWEEN CONCAT(#{param.startDate},' 00:00:00') AND CONCAT(#{param.endDate},' 23:59:59') 
            GROUP BY 
                tlal.full_short_url, tl.gid, tlal.ip 
            ORDER BY 
                count DESC 
            LIMIT 5;
            """)
    List<HashMap<String, Object>> listTopIpByShortLink(@Param("param") ShortLinkStatsDTO requestParam);

    /**
     * 根据分组获取指定日期内高频访问IP数据
     */
    @Select("""
            SELECT 
                tlal.ip, 
                COUNT(tlal.ip) AS count 
            FROM 
                t_link tl INNER JOIN 
                t_link_access_logs tlal ON tl.full_short_url = tlal.full_short_url 
            WHERE 
                tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlal.create_time BETWEEN CONCAT(#{param.startDate},' 00:00:00') AND CONCAT(#{param.endDate},' 23:59:59')
            GROUP BY 
                tl.gid, tlal.ip 
            ORDER BY 
                count DESC 
            LIMIT 5;
            """)
    List<HashMap<String, Object>> listTopIpByGroup(@Param("param") ShortLinkGroupStatsDTO requestParam);


    /**
     * 根据短链接获取指定日期内新旧访客数据
     */
    @Select("""
            SELECT 
                SUM(old_user) AS oldUserCnt, 
                SUM(new_user) AS newUserCnt 
            FROM ( 
                SELECT 
                    CASE WHEN COUNT(DISTINCT DATE(tlal.create_time)) > 1 THEN 1 ELSE 0 END AS old_user, 
                    CASE WHEN COUNT(DISTINCT DATE(tlal.create_time)) = 1 AND MAX(tlal.create_time) >= CONCAT(#{param.startDate},' 00:00:00') AND MAX(tlal.create_time) <= CONCAT(#{param.endDate},' 23:59:59') THEN 1 ELSE 0 END AS new_user 
                FROM 
                    t_link tl INNER JOIN 
                    t_link_access_logs tlal ON tl.full_short_url = tlal.full_short_url 
                WHERE 
                    tlal.full_short_url = #{param.fullShortUrl} 
                    AND tl.gid = #{param.gid} 
                    AND tl.enable_status = '0'
                    AND tl.del_flag = '0' 
                GROUP BY 
                    tlal.user 
            ) AS user_counts;
            """)
    HashMap<String, Object> findUvTypeCntByShortLink(@Param("param") ShortLinkStatsDTO requestParam);


    /**
     * 获取用户信息是否新老访客
     */
    // 加<script>标签是因为sql语句中有动态标签<foreach>
    @Select("""
            <script> 
            SELECT 
                tlal.user, 
                CASE 
                    WHEN MIN(tlal.create_time) BETWEEN CONCAT(#{startDate},' 00:00:00') AND CONCAT(#{endDate},' 23:59:59') THEN '新访客' 
                    ELSE '老访客' 
                END AS uvType 
            FROM 
                t_link tl INNER JOIN 
                t_link_access_logs tlal ON tl.full_short_url = tlal.full_short_url 
            WHERE 
                tlal.full_short_url = #{fullShortUrl} 
                AND tl.gid = #{gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0'
                AND tlal.user IN 
                <foreach item='item' index='index' collection='userAccessLogsList' open='(' separator=',' close=')'> 
                    #{item} 
                </foreach> 
            GROUP BY 
                tlal.user;
            </script>
            """)
    List<Map<String, Object>> selectUvTypeByUsers(@Param("gid") String gid,
                                                  @Param("fullShortUrl") String fullShortUrl,
                                                  @Param("startDate") String startDate,
                                                  @Param("endDate") String endDate,
                                                  @Param("userAccessLogsList") List<String> userAccessLogsList);

    /**
     * 获取分组用户信息是否新老访客
     */
    @Select("""
            <script> 
            SELECT 
                tlal.user, 
                CASE 
                    WHEN MIN(tlal.create_time) BETWEEN CONCAT(#{startDate},' 00:00:00') AND CONCAT(#{endDate},' 23:59:59') THEN '新访客' 
                    ELSE '老访客' 
                END AS uvType 
            FROM 
                t_link tl INNER JOIN 
                t_link_access_logs tlal ON tl.full_short_url = tlal.full_short_url 
            WHERE 
                tl.gid = #{gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlal.user IN 
                <foreach item='item' index='index' collection='userAccessLogsList' open='(' separator=',' close=')'> 
                    #{item} 
                </foreach> 
            GROUP BY 
                tlal.user;
            </script>
            """)
    List<Map<String, Object>> selectGroupUvTypeByUsers(
            @Param("gid") String gid,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("userAccessLogsList") List<String> userAccessLogsList
    );

    /**
     * 根据短链接获取指定日期内PV、UV、UIP数据
     */
    @Select("""
            SELECT 
                COUNT(tlal.user) AS pv, 
                COUNT(DISTINCT tlal.user) AS uv, 
                COUNT(DISTINCT tlal.ip) AS uip 
            FROM 
                t_link tl INNER JOIN 
                t_link_access_logs tlal ON tl.full_short_url = tlal.full_short_url 
            WHERE 
                tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlal.create_time BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59')
            GROUP BY 
                tl.gid;
            """)
    LinkAccessStats findPvUvUidStatsByShortLink(@Param("param") ShortLinkStatsDTO requestParam);


    /**
     * 根据分组获取指定日期内PV、UV、UIP数据
     */
    @Select("""
            SELECT 
                COUNT(tlal.user) AS pv, 
                COUNT(DISTINCT tlal.user) AS uv, 
                COUNT(DISTINCT tlal.ip) AS uip 
            FROM 
                t_link tl INNER JOIN 
                t_link_access_logs tlal ON tl.full_short_url = tlal.full_short_url 
            WHERE 
                tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlal.create_time BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59')
            GROUP BY 
                tl.gid;
            """)
    LinkAccessStats findPvUvUidStatsByGroup(@Param("param") ShortLinkGroupStatsDTO requestParam);

    @Select("""
            SELECT 
                tlal.* 
            FROM 
                t_link tl 
                INNER JOIN t_link_access_logs tlal ON tl.full_short_url = tlal.full_short_url 
            WHERE 
                tl.gid = #{param.gid} 
                AND tl.del_flag = '0' 
                AND tl.enable_status = '0' 
                AND tlal.create_time BETWEEN CONCAT(#{param.startDate},' 00:00:00') and CONCAT(#{param.endDate},' 23:59:59') 
            ORDER BY 
                tlal.create_time DESC
            """)
    IPage<LinkAccessLogs> selectGroupPage(@Param("param") ShortLinkGroupStatsAccessRecordDTO requestParam);
}
