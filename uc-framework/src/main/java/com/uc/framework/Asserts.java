//package com.uc.framework;
//
//import java.util.Collection;
//import java.util.Map;
//import org.springframework.util.CollectionUtils;
//import com.uc.framework.obj.BusinessException;
//
///***
// * ���Թ�������
// * <p>
// * Description:
// * </p>
// * 
// * @author HadLuo
// * @date 2020-9-2 10:52:45
// */
//public class Asserts {
//
//    /**
//     * ���� ���� �ǲ���Ϊ�գ� Ϊ�վ��׳� BusinessException
//     * 
//     * @param target
//     * @param errorMessage
//     */
//    public static void assertNull(Object target, String errorMessage) {
//        if (null == target) {
//            throw new BusinessException(errorMessage);
//        }
//        if (target instanceof Collection) {
//            if (CollectionUtils.isEmpty((Collection<?>) target)) {
//                throw new BusinessException(errorMessage);
//            }
//        }
//        if (target instanceof Map<?, ?>) {
//            if (((Map<?, ?>) target).isEmpty()) {
//                throw new BusinessException(errorMessage);
//            }
//        }
//    }
//
//}
