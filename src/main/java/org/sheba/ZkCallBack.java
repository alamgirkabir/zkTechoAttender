package org.sheba;

import java.nio.ByteBuffer;

public interface ZkCallBack<T> {

    public T callBack(boolean status, ByteBuffer buffer);

}
