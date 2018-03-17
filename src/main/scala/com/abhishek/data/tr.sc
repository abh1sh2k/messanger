case class Connection(val con : Int)

case class Query(val qu :Int)
def getValuefromQuery: Query => Connection => Int = {
  query : Query => getValuefromConnection(query)
}
def getValuefromConnection(query: Query): Connection => Int ={
  conn : Connection => conn.con
}
implicit val a = new Connection(5)
implicit val b = new Query(15)
def check = {
  getValuefromQuery {
    a
  }
}



