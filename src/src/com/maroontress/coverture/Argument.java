package com.maroontress.coverture;

/**
   �����Ǥ���
*/
public class Argument {

    /** �������ͤޤ��ϥ��ץ�����̾�� */
    private String name;

    /** ���ץ������͡��ޤ���null */
    private String value;

    /** ���ץ����Ǥ������true */
    private boolean option;

    /**
       �������������ޤ���

       @param arg ����
    */
    public Argument(final String arg) {
	name = arg;
	value = null;
	option = false;
    }

    /**
       ���ץ����ΰ������������ޤ���

       @param name ���ץ�����̾��
       @param value ���ץ�������
    */
    public Argument(final String name, final String value) {
	this.name = name;
	this.value = value;
	option = true;
    }

    /**
       ���������ץ���󤫤ɤ�����������ޤ���

       @return ���ץ����ξ���true�������Ǥʤ����false
    */
    public boolean isOption() {
	return option;
    }

    /**
       �������ͤ�������ޤ���

       ���������ץ����ξ��ϥ��ץ�����̾����������ޤ���

       @return �������ͤޤ��ϥ��ץ�����̾��
    */
    public String getName() {
	return name;
    }

    /**
       ���ץ������ͤ�������ޤ���

       @return ���ץ������ͤޤ���null
    */
    public String getValue() {
	return value;
    }
}
