package com.cyh.sy.mq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * @描述：消息队列监听
 * @作者：CYH
 * @版本：V1.0
 * @创建时间：：2016-12-19 上午10:26:05
 */
public class QueueMessageListener implements MessageListener {

    //当收到消息后，自动调用该方法
    @Override
    public void onMessage(Message message) {
        TextMessage tm = (TextMessage) message;
        try {
            System.out.println("QueueMessageListener监听到了文本消息："+ tm.getText());
            //do something ...
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}