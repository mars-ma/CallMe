package dev.mars.callme.utils;

/**
 * 处理基本类型间的转换
 * @author ma.xuanwei
 *
 */
public class BasicTypeConvertUtils {

	/**
	 * 将字节数组转换为short
	 * @param b
	 * @return
	 */
	public static short byteToShort(byte[] b) {
		return (short) (((b[0] & 0xff) << 8) | (b[1] & 0xff));
	}

	/**
	 * 将字节数组转换为short
	 * @param b1 低位字节对应高位
	 * @param b2 高位字节对应低位
	 * @return
	 */
	public static short byteToShort(byte b1,byte b2) {
		return (short) (((b1 & 0xff) << 8) | (b2 & 0xff));
	}
}
