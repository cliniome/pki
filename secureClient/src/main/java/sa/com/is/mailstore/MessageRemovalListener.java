package sa.com.is.mailstore;

import sa.com.is.Message;

public interface MessageRemovalListener {
    public void messageRemoved(Message message);
}
