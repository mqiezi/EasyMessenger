package com.tech618.easymessengersample;

import android.os.Parcelable;
import android.os.RemoteException;

import com.tech618.easymessenger.BinderInterface;
import com.tech618.easymessengerclientservercommon.Color;
import com.tech618.easymessengerclientservercommon.User;

import java.util.List;

/**
 * Created by zmy on 2018/4/7.
 */
@BinderInterface
public interface ITestFunction
{
    void voidTest() throws RemoteException;

    int intTest(int num1, int num2) throws RemoteException;

    byte byteTest(byte b1, byte b2) throws RemoteException;

    long longTest(long l1, long l2) throws RemoteException;

    float floatTest(float f1, float f2) throws RemoteException;

    boolean booleanTest(boolean b) throws RemoteException;

    String stringTest(String s1, String s2) throws RemoteException;

    User parcelableTest(User user) throws RemoteException;

    List<Integer> primitiveListTest(List<Integer> list) throws RemoteException;

    List<User> typeListTest(List<User> list) throws RemoteException;

    Color enumTest(Color color) throws RemoteException;

    User nullTest(User user) throws RemoteException;
}

