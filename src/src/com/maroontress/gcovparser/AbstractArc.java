package com.maroontress.gcovparser;

/**
   �ؿ�����դΥ���������ݥ��饹�Ǥ���
*/
public abstract class AbstractArc {

    /**
       ���������������ޤ���
    */
    protected AbstractArc() {
    }

    /**
       �����������ѥ˥󥰥ĥ꡼�������뤫�ɤ����������ޤ���

       @return ���ѥ˥󥰥ĥ꡼�����������true�������Ǥʤ����
       false
    */
    public abstract boolean isOnTree();

    /**
       �����������Υ��������ɤ����������ޤ���

       @return ���Υ������ξ���true�������Ǥʤ����false
    */
    public abstract boolean isFake();

    /**
       �¹Բ�����ɲä��ޤ���

       @param delta �ɲä���¹Բ��
    */
    public abstract void addCount(long delta);

    /**
       �¹Բ����������ޤ���

       @return �¹Բ��
    */
    public abstract long getCount();
}
