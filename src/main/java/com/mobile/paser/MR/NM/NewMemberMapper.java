package com.mobile.paser.MR.NM;

import com.mobile.Util.MemberUtil;
import com.mobile.Util.TimeUtil;
import com.mobile.common.DateTypeEnum;
import com.mobile.common.kpiTypeEnum;
import com.mobile.paser.modle.dim.StatsCommonDimension;
import com.mobile.paser.modle.dim.base.*;
import com.mobile.paser.modle.dim.value.map.TimeOutPutWriteable;
import com.mobile.paser.service.DimensionOperateI;
import com.mobile.paser.service.serviceImpl.DimensionOPerateImpl;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;

/**
 * @Auther: 72428
 * @Date: 2018/12/5 21:57
 * @Description:
 *      解析一行日志
 *      得到的结果为 StatsUserDiemension-》包含（DateDimension，PlatfromDimension，KpiDimension）
 *      DateDimension 用户登录或者注册的时间-》解析为年月日天
 *      PlatfromDimension 用户登录和注册的平台
 *      KpiDimension kpi 指标（new_user,browser_new_user,active_user）
 *
 *      TimeOutPutWriteable uid \ mid \sid
 */
public class NewMemberMapper extends Mapper<LongWritable,Text,StatsUserDiemension,TimeOutPutWriteable> {
    public static final Logger logger=Logger.getLogger(NewMemberMapper.class);
    private StatsUserDiemension statsUserDiemension=new StatsUserDiemension();
    private DimensionOperateI dimensionOperateI=new DimensionOPerateImpl();
    private StatsCommonDimension commonDimension=new StatsCommonDimension();
    private TimeOutPutWriteable outPutWriteable=new TimeOutPutWriteable();
    private Connection conn=null;
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        String line = value.toString();
        String[] split = line.split("\001");
        String memberId=split[8];
        String time=split[10];

        if(MemberUtil.checkMeberId(memberId,conn,context.getConfiguration())){
            logger.info("memberId is not new memberId.memberId:"+memberId);
            return;
        }
        long aLong = TimeUtil.string2Long(time);
        //k->DateDimension
        DateDimension dateDimension = DateDimension.buildDate(aLong, DateTypeEnum.DAY);
        int dateID=dimensionOperateI.getDimensionIdByDimension(dateDimension);
        dateDimension.setId(dateID);
        //k->PlatfromDimension
        PlatfromDimension platfromDimension=new PlatfromDimension(split[2]);
        int platID=dimensionOperateI.getDimensionIdByDimension(platfromDimension);
        platfromDimension.setId(platID);
        //k->KpiDimension
        KpiDimension kpiDimension = new KpiDimension(kpiTypeEnum.BROWSER_NEW_MEMBER.kpiName);
        int kpiID=dimensionOperateI.getDimensionIdByDimension(kpiDimension);
        kpiDimension.setId(kpiID);

        //k->BrowserDiemenson
        BrowserDiemenson browserDiemenson = new BrowserDiemenson(split[28],split[29]);
        int browerId=dimensionOperateI.getDimensionIdByDimension(browserDiemenson);
        browserDiemenson.setId(browerId);

        //value->TimeOutPutWriteable
        outPutWriteable.setId(memberId);
        outPutWriteable.setTime(Long.valueOf(split[24]));

        //StatsCommonDimension 获取到值
        commonDimension.setDateDimension(dateDimension);
        commonDimension.setPlatfromDimension(platfromDimension);
        commonDimension.setKpiDimension(kpiDimension);
        //StatsUserDiemension获取到值
        statsUserDiemension.setBrowserDiemenson(browserDiemenson);
        statsUserDiemension.setStatsCommonDimension(commonDimension);

        context.write(statsUserDiemension, outPutWriteable);
//----------------------------------------------------------------------------------------------------------------------
        //no BrowserDiemenson  new_user
        KpiDimension kpinewUser = new KpiDimension(kpiTypeEnum.NEW_MEMBER.kpiName);
        kpiID=dimensionOperateI.getDimensionIdByDimension(kpinewUser);
        kpinewUser.setId(kpiID);
        BrowserDiemenson defualtBrower = new BrowserDiemenson("","");
        commonDimension.setKpiDimension(kpinewUser);
        commonDimension.setPlatfromDimension(platfromDimension);
        commonDimension.setDateDimension(dateDimension);
        statsUserDiemension.setBrowserDiemenson(defualtBrower);
        statsUserDiemension.setStatsCommonDimension(commonDimension);
        context.write(statsUserDiemension, outPutWriteable);

    }
}
