package br.com.cdsoft.kafka;

import br.com.cdsoft.domain.Fruit;

public class MessageFruit {
    private Fruit fruit;
    private MessageType messageType;

    public MessageFruit(Fruit fruit, MessageType messageType) {
        this.fruit = fruit;
        this.messageType = messageType;
    }

    public Fruit getFruit() {
        return fruit;
    }

    @Override
    public String toString() {
        return "MessageFruit{" +
                "fruit=" + fruit +
                ", messageType=" + messageType +
                '}';
    }


    public enum MessageType {

        UPDATE,
        INSERT,
        DELETE;

    }
}
