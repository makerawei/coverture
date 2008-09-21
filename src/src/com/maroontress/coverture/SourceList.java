package com.maroontress.coverture;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

/**
   �ҤȤĤ�gcno�ե����뤬���Ȥ��륽�����ե�����Υꥹ�ȤǤ���
*/
public final class SourceList {

    /** �������ե�����Υѥ��ȥ������ΥޥåפǤ��� */
    private HashMap<String, Source> map;

    /**
       �������ꥹ�Ȥ��������ޤ���
    */
    public SourceList() {
	map = new HashMap<String, Source>();
    }

    /**
       �������ե�����Υѥ����б����륽������������ޤ���

       @param sourceFile �������ե�����Υѥ�
       @return ������
    */
    public Source getSource(final String sourceFile) {
	Source source = map.get(sourceFile);
	if (source == null) {
	    source = new Source(sourceFile);
	    map.put(sourceFile, source);
	}
	return source;
    }
    
    /**
    */
    public void ouputFiles(final String pathPrefix, final long timestamp) {
	Collection<Source> all = map.values();
	for (Source s : all) {
	    try {
		s.outputFile(pathPrefix, timestamp);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
}
