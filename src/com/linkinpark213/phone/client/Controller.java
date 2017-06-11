package com.linkinpark213.phone.client;

import com.linkinpark213.phone.client.receiver.ReceiverThread;
import com.linkinpark213.phone.common.Message;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;


/**
 * Created by ooo on 2017/5/2 0002.
 */
public class Controller {
    private Text statusText;
    private Text localStatusText;
    private Button callButton;
    private Button hangButton;
    private Dialer dialer;
    private Conversation conversation;
    private Socket conversationSocket;
    private AnswerListenerThread answerListenerThread;
    private int currentState;
    public static final int IN_CONVERSATION = 0;
    public static final int WAITING_FOR_CALL = 1;
    public static final int WAITING_FOR_ANSWER = 2;
    public static final int CALL_INCOMING = 3;

    public Controller(Text statusText,
                      Text localStatusText,
                      Button callButton,
                      Button hangButton) {
        this.statusText = statusText;
        this.localStatusText = localStatusText;
        this.callButton = callButton;
        this.hangButton = hangButton;
        CallInListenerThread callInListenerThread = new CallInListenerThread(this);
        currentState = WAITING_FOR_CALL;
        callInListenerThread.start();
        this.dialer = new Dialer();
    }

    public void callIncoming(Socket socket) {
        statusText.setText("Call incoming from " + socket.getRemoteSocketAddress() + socket.getPort());
        this.currentState = CALL_INCOMING;
        conversationSocket = socket;
        hangButton.setDisable(false);
    }

    public void answerCall() {
        statusText.setText("You Answered the Phone.");
        System.out.println("You Answered the Phone.");
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(conversationSocket.getOutputStream());
            objectOutputStream.writeObject(new Message(Message.ANSWER, ""));
            startConversation();
        } catch (IOException e) {
            e.printStackTrace();
        }
        callButton.setDisable(true);
        hangButton.setDisable(false);
    }

    public void cancelDialing() {
        if (this.currentState == IN_CONVERSATION) {
            hangOff();
        } else
            this.currentState = WAITING_FOR_CALL;
    }

    public void hangOff() {
        currentState = WAITING_FOR_CALL;
        statusText.setText("You Hung Off.");
        System.out.println("You Hung Off.");
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(conversationSocket.getOutputStream());
            objectOutputStream.writeObject(new Message(Message.HANG_OFF, ""));
            conversationSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        callButton.setDisable(false);
        hangButton.setDisable(true);
    }

    public void callingEnd() {
        currentState = WAITING_FOR_CALL;
        statusText.setText("The Other User Hung Off.");
        System.out.println("The Other User Hung Off.");
        try {
            conversationSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        callButton.setDisable(false);
        hangButton.setDisable(true);
    }

    public void refuseToAnswer() {
        statusText.setText("You Refused to Answer.");
        System.out.println("You Refused to Answer.");
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(conversationSocket.getOutputStream());
            objectOutputStream.writeObject(new Message(Message.CALL_REFUSE, ""));
//            conversationSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentState = WAITING_FOR_CALL;
        hangButton.setDisable(true);
    }

    public boolean isListening() {
        return currentState == WAITING_FOR_CALL;
    }

    public boolean isBeingCalled() {
        return currentState == CALL_INCOMING;
    }

    public boolean isWaitingForAnswer() {
        return currentState == WAITING_FOR_ANSWER;
    }

    public boolean isInConversation() {
        return currentState == IN_CONVERSATION;
    }

    public void keepListening() {
        this.currentState = WAITING_FOR_CALL;
    }

    public void waitForCall() {
        this.currentState = WAITING_FOR_CALL;
        callButton.setDisable(false);
        hangButton.setDisable(true);
    }

    public void callingRefused() {
        waitForCall();
    }

    public void waitForAnswer(Socket socket) {
        this.currentState = WAITING_FOR_ANSWER;
        this.conversationSocket = socket;
        answerListenerThread = new AnswerListenerThread(this);
        answerListenerThread.start();
        callButton.setDisable(true);
        hangButton.setDisable(false);
    }

    public void startConversation() {
        conversation = new Conversation(conversationSocket);
        ConversationControlThread conversationControlThread = new ConversationControlThread(conversation, this);
        ReceiverThread receiverThread = new ReceiverThread(conversation, this);
        conversationControlThread.start();
        receiverThread.start();
        this.currentState = IN_CONVERSATION;
        statusText.setText("Conversation Established with " + conversationSocket.getRemoteSocketAddress());
        System.out.println("Conversation Established with " + conversationSocket.getRemoteSocketAddress());
        callButton.setDisable(true);
        hangButton.setDisable(false);
    }

    public void setStatus(String status) {
        statusText.setText(status);
    }

    public Socket getConversationSocket() {
        return conversationSocket;
    }

    public int getCurrentState() {
        return currentState;
    }

    public Text getLocalStatusText() {
        return localStatusText;
    }

    public void setLocalStatus(String localStatus) {
        localStatusText.setText(localStatus);
    }

    public boolean dial(String address, int port) {
        Socket socket = dialer.dial(address, port);
        System.out.println("Local Address & Port: " + socket.getLocalAddress() + ":" + socket.getLocalPort());
        if (socket != null) {
            this.waitForAnswer(socket);
            return true;
        } else {
            System.out.println("Dialing Error: No Answerer.");
            statusText.setText("Dialing Error: No Answerer.");
            return false;
        }
    }
}
