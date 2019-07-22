// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A step defines something which is done. A step has always a number and maybe knows the next step (number).
 * The annoation alone makes not much sense - it shall be used in other annotations as a parameter! It also has description field an a name
 * @author Albert Tregnaghi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Step{
	
	/**
	 * Using this as the only next step will mark the current step as terminated without having a next one.
	 */
	public static final int NO_NEXT_STEP=0;
	
	/**
	 * Define the step number. Interesting
	 * when there are multiple methods annotated with an use case
	 * annotation. 
	 * <br><br> 
	 * Value must be >0 . The order with the lowest
	 * value is the first one. 
	 * 
	 * @return order value
	 */	
	int number();
	
	/**
	 * Define the numbers of the next steps to follow up. if there are more than one step defined, this means the step execution is branched.
	 * Per default no next steps are defined, so natural ordering is used and if there is another step with an higher number this will be assumed to be
	 * the next one. If the only entry inside this array is ( {@link Step#NO_NEXT_STEP}, this is meant to have the step ends here without calling another step!
	 * @return
	 */
	int[] next() default {};
	
	/**
	 * A description what the usecase step is for. Should be done in asciidoctor syntax.
	 * If the descriptions ends with ".adoc" its assumed that this is a file name instead!
	 * <br><br>
	 * For example: <br><code>description="usecases/user/signupUser_step1.adoc"</code> <br>will be tried by asciidoctor
	 * generator as an include of this file instead of just inserting it!
	 * @return description text or filename or an empty string when not defined
	 */
	String description() default "";
	
	/**
	 * Name for this step
	 * @return
	 */
	String name();
	
	/**
	 * A description about the need of REST documentation for this step. By this information an automated scan for missing REST doc parts can be done!<br><br>
	 * Please do not use it with false, because this is the default, by having only references having this enabled it is very easy to find the restdoc parts in code
	 * by searchin references with your IDE...
	 * @return <code>true</code> when for this step a rest documentation is necessary
	 */
	boolean needsRestDoc() default false;
}
