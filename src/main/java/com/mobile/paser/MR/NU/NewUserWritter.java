package com.mobile.paser.MR.NU;

import com.mobile.common.GlobalConstans;
import com.mobile.common.kpiTypeEnum;
import com.mobile.paser.MR.ReduceOutPutFormatI;
import com.mobile.paser.modle.dim.StatsBaseDimension;
import com.mobile.paser.modle.dim.base.StatsUserDiemension;
import com.mobile.paser.modle.dim.value.StatsBaseOutputWritable;
import com.mobile.paser.modle.dim.value.reduce.ReduceOutPUtWritable;
import com.mobile.paser.service.DimensionOperateI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Auther: 72428
 * @Date: 2018/12/7 11:07
 * @Description: 为新增用户的ps赋值
 */
public class NewUserWritter implements ReduceOutPutFormatI {
    @Override
    public void outputWritter(Configuration conf, StatsBaseDimension key, StatsBaseOutputWritable value, PreparedStatement ps, DimensionOperateI convert) {
        ReduceOutPUtWritable v= (ReduceOutPUtWritable) value;
        StatsUserDiemension k= (StatsUserDiemension) key;
        int newuser =((IntWritable)v.getValue().get(new IntWritable(-1))).get();
        //new_browser_user
        int i=0;
        if(v.getKpi().kpiName.equals(kpiTypeEnum.BROWSER_NEW_USER.kpiName)) {
            try {
                ps.setInt(++i, convert.getDimensionIdByDimension(k.getStatsCommonDimension().getDateDimension()));
                ps.setInt(++i, convert.getDimensionIdByDimension(k.getStatsCommonDimension().getPlatfromDimension()));
                ps.setInt(++i, convert.getDimensionIdByDimension(k.getBrowserDiemenson()));
                ps.setInt(++i, newuser);
                ps.setString(++i, conf.get(GlobalConstans.RUNNING_DATE));
                ps.setInt(++i, newuser);

                ps.addBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else {
            try {
                ps.setInt(++i, convert.getDimensionIdByDimension(k.getStatsCommonDimension().getDateDimension()));
                ps.setInt(++i, convert.getDimensionIdByDimension(k.getStatsCommonDimension().getPlatfromDimension()));
                ps.setInt(++i, newuser);
                ps.setString(++i, conf.get(GlobalConstans.RUNNING_DATE));
                ps.setInt(++i, newuser);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }
}
