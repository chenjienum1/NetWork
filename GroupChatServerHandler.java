package com.attencent.netty.groupchat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class GroupChatServerHandler extends SimpleChannelInboundHandler<String> {

    //使用一个hashMap惯例
    public static Map<String,Channel> channels=new HashMap<>();
    //public static Map<User,Channel> channels2=new HashMap<>();

    //定义一个channel组，管理所有的channel
    //GlobalEventExecutor.INSTANCE)是全局的事件执行器，是一个单例
    private static ChannelGroup channelGroup=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //handlerAdder 表示连接建立，一旦连接，第一个被执行
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel=ctx.channel();
        //将该客户加u人聊天的信息推送给其他在线的客户端
        /*
        该方法会将 channelGroup 中所有的channel 遍历，并发送消息
        我们不需要自己遍历
         */
        channelGroup.writeAndFlush("[客户端]"+channel.remoteAddress()+"加入聊天\n"
                +sdf.format(new java.util.Date())+"\n");
        channelGroup.add(channel);

        channels.put("id100",channel);
        //channels2.put(new User(10,"123"),channel);

    }

    //表示channel 处于活动状态，提示xx上线
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        System.out.println(ctx.channel().remoteAddress()+"上线了~");
    }

    //表示channel处于不活动状态，提示xx离线了
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+"离线了~");
    }

    //断开连接,将xx客户离开信息推送给当前在线的客户
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.writeAndFlush("[客户端]"+channel.remoteAddress()+"离开了\n");
        System.out.println("channelGroup size "+channelGroup.size());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        //获取到当前的channel
        Channel channel=ctx.channel();
        //这时我们遍历channelGroup,根据不同的情况，返回不同的消息

        channelGroup.forEach(ch->{
            if (channel !=ch){ //不是当前的channel,转发消息
                ch.writeAndFlush("[客户]"+channel.remoteAddress()+"发送消息:"+msg+"\n");

            }else {//回显自己发送的消息给自己
                ch.writeAndFlush("[自己发送的消息]"+msg+"\n");
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //关闭通道
        ctx.close();
    }
}
