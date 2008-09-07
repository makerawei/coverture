package com.maroontress.coverture;

/**
   �����Υѡ����Ǥ���
*/
public class ArgumentParser {

    /** ���������� */
    private String[] av;

    /** ���˼�����������ΰ��� */
    private int position;

    /**
       �����Υѡ������������ޤ���

       @param av ����������
    */
    public ArgumentParser(final String[] av) {
	this.av = av;
	position = 0;
    }

    /**
       ������������ޤ���

       @return ����
    */
    public Argument getArgument() {
	if (position == av.length) {
	    return null;
	}
	String s = av[position];
	++position;
	if (!s.startsWith("-")) {
	    return new Argument(s);
	}
	int n = s.indexOf('=');
	if (n < 0) {
	    return new Argument(s, null);
	}
	return new Argument(s.substring(0, n), s.substring(n + 1));
    }
}
