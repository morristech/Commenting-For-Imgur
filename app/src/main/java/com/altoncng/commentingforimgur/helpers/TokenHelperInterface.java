package com.altoncng.commentingforimgur.helpers;

/**
 * Created by Eye on 10/3/2015.
 */
public interface TokenHelperInterface {
    void processFinish(String output, int task, int errCode);
    void tokensReceived(String username, boolean success, int errCode);
}
