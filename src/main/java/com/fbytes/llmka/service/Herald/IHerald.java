package com.fbytes.llmka.service.Herald;

import com.fbytes.llmka.model.heraldmessage.HeraldMessage;

@FunctionalInterface
public interface IHerald<T extends HeraldMessage> {

    void sendMessage(T msg) throws SendMessageException;

    public abstract class SendMessageException extends Exception{}

    public class SendMessageExceptionTemporary extends SendMessageException{}

    public class SendMessageExceptionPermanent extends SendMessageException {}

}
