package strategy;

/***
 * 
 * 定义鸭子吱吱叫的行为
 * 
 */

public class Squeak implements QuackBehavior {
	public void quack() {
		System.out.println("吱吱叫");
	}
}
