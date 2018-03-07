trait A
{
  protected val m = Map(1->2)
}
class A1 extends A {
  def check: Unit = {
    println(m)
  }
}
class A2 extends A1 {
  def check1: Unit = {
    println(m)
  }
}
class A3 extends A2 {
  def check2: Unit = {
    println(m)
  }
}
val a = new A3
a.check2