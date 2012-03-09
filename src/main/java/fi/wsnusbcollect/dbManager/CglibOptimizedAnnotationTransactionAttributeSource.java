/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.dbManager;

import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
* Description
*
* @author Jan Novotný, FG Forrest a.s. (c) 2007
* @version $Id: $
*/
public class CglibOptimizedAnnotationTransactionAttributeSource extends AnnotationTransactionAttributeSource {
    private static final Logger log = LoggerFactory.getLogger(CglibOptimizedAnnotationTransactionAttributeSource.class);
 
    @Override
    protected TransactionAttribute findTransactionAttribute(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (AopUtils.isCglibProxyClass(declaringClass)) {
            try {
                //find appropriate method on parent class
                Method superMethod = declaringClass.getSuperclass().getMethod(method.getName(), method.getParameterTypes());
                return super.findTransactionAttribute(superMethod);
            } catch (Exception ex) {
                if(log.isWarnEnabled()) {
                    log.warn("Cannot find superclass method for Cglib method: " + method.toGenericString());
                }
                return super.findTransactionAttribute(method);
            }
        } else {
            return super.findTransactionAttribute(method);
        }
    }
 
}