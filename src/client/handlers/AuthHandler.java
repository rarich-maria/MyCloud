package client.handlers;
import client.controller.impl.ClientEventController;
import common.message.AbstractMessage;
import common.message.AuthMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private String userName;
    private ClientEventController eventController;

    public AuthHandler(ClientEventController eventController) {
        this.eventController = eventController;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        AbstractMessage mes = (AbstractMessage)msg;
        if (mes instanceof AuthMessage) {
            AuthMessage authMsg = (AuthMessage) mes;
            if (!authMsg.isAuthSuccessfull()) {
                eventController.authorizationIsFailed();
            }else {
                userName = authMsg.getUserName();
                eventController.authorizationIsPass(userName);
                changePipeline(ctx);
            }
        }
    }

    private void changePipeline(ChannelHandlerContext ctx) {
        ctx.pipeline().remove(this.getClass());
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        cause.printStackTrace();
    }
}
