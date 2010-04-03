package com.maroontress.coverture;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
   gcno/gcda�ե������ѡ������뤿��Υ桼�ƥ���ƥ����饹�Ǥ���
*/
public final class Parser {

    /** INT32�ΥХ��ȥ������Ǥ��� */
    public static final int SIZE_INT32 = 4;

    /**
       ���󥹥ȥ饯���Ǥ���
    */
    private Parser() {
    }

    /**
       �Х��ȥХåե�����ʸ��������Ϥ������Υ��󥹥��󥹤��֤��ޤ���
       ���󥹥��󥹤�Stirng.intern()���֤�ʸ����Ǥ���ʸ�����Ĺ����0
       �ΤȤ���null���֤��ޤ����̥륿���ߥ͡��ȤΤ����0������ӥѥǥ�
       �󥰤�0�ϥ����åפ��졢�Х��ȥХåե��ΰ��֤�ʸ����μ��ΰ��֤�
       �ʤߤޤ���

       string: int32:0 | int32:length char* char:0 padding
       padding: | char:0 | char:0 char:0 | char:0 char:0 char:0

       @param bb �Х��ȥХåե�
       @return ʸ����
       @throws IOException �����ϥ��顼
    */
    public static String getString(final ByteBuffer bb) throws IOException {
	int length = bb.getInt();
	if (length == 0) {
	    return null;
	}
	byte[] bytes = new byte[length * SIZE_INT32];
	bb.get(bytes);

	int k;
	for (k = 0; k < bytes.length && bytes[k] != 0; ++k) {
	    continue;
	}
	return new String(bytes, 0, k).intern();
    }
}
