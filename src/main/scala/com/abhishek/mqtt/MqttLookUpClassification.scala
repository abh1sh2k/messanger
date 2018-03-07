//package com.abhishek.mqtt
//
//import java.util.Comparator
//import java.util.concurrent.{ConcurrentHashMap, ConcurrentSkipListSet}
//
//import akka.event.EventBus
//
//import scala.annotation.tailrec
//import scala.collection.JavaConverters.{ asScalaIteratorConverter, collectionAsScalaIterableConverter }
//
///**
//  * Created by abhishek on 03/03/18
//  */
//trait MqttLookUpClassification { this: EventBus ⇒
//
//  protected final val subscribers = new ConcurrentBiDirectionMap[Classifier, Subscriber](mapSize(), new Comparator[Subscriber] {
//    def compare(a: Subscriber, b: Subscriber): Int = compareSubscribers(a, b)
//  })
//
//  /**
//    * This is a size hint for the number of Classifiers you expect to have (use powers of 2)
//    */
//  protected def mapSize(): Int
//
//  /**
//    * Provides a total ordering of Subscribers (think java.util.Comparator.compare)
//    */
//  protected def compareSubscribers(a: Subscriber, b: Subscriber): Int
//
//  /**
//    * Returns the Classifier associated with the given Event
//    */
//  protected def classify(event: Event): Classifier
//
//  /**
//    * Publishes the given Event to the given Subscriber
//    */
//  protected def publish(event: Event, subscriber: Subscriber): Unit
//
//  def subscribe(subscriber: Subscriber, to: Classifier): Boolean = subscribers.put(to, subscriber)
//
//  def unsubscribe(subscriber: Subscriber, from: Classifier): Boolean = subscribers.remove(from, subscriber)
//
//  def unsubscribe(subscriber: Subscriber): Unit = subscribers.removeValue(subscriber)
//
//  def publish(event: Event): Unit = {
//    val i = subscribers.valueIterator(classify(event))
//    while (i.hasNext) publish(event, i.next())
//  }
//}
//class ConcurrentBiDirectionMap[K, V](val mapSize: Int, val valueComparator: Comparator[V]) {
//
//  def this(mapSize: Int, cmp: (V, V) ⇒ Int) = this(mapSize, new Comparator[V] {
//    def compare(a: V, b: V): Int = cmp(a, b)
//  })
//
//  private val classifiersToSubscribers = new ConcurrentHashMap[K, ConcurrentSkipListSet[V]](mapSize)
//  private val emptySet = new ConcurrentSkipListSet[V]
//  private val subscribersToClassifiers = new ConcurrentHashMap[V, ConcurrentSkipListSet[V]](mapSize)
//
//
//  /**
//    * Associates the value of type V with the key of type K
//    * @return true if the value didn't exist for the key previously, and false otherwise
//    */
//  def put(key: K, value: V): Boolean = {
//    //Tailrecursive spin-locking put
//    @tailrec
//    def spinPut(k: K, v: V): Boolean = {
//      var retry = false
//      var added = false
//      val set = classifiersToSubscribers get k
//
//      if (set ne null) {
//        set.synchronized {
//          if (set.isEmpty) retry = true //IF the set is empty then it has been removed, so signal retry
//          else { //Else add the value to the set and signal that retry is not needed
//            added = set add v
//            retry = false
//          }
//        }
//      } else {
//        val newSet = new ConcurrentSkipListSet[V](valueComparator)
//        newSet add v
//
//        // Parry for two simultaneous putIfAbsent(id,newSet)
//        val oldSet = classifiersToSubscribers.putIfAbsent(k, newSet)
//        if (oldSet ne null) {
//          oldSet.synchronized {
//            if (oldSet.isEmpty) retry = true //IF the set is empty then it has been removed, so signal retry
//            else { //Else try to add the value to the set and signal that retry is not needed
//              added = oldSet add v
//              retry = false
//            }
//          }
//        } else added = true
//      }
//
//      if (retry) spinPut(k, v)
//      else added
//    }
//
//    spinPut(key, value)
//  }
//
//  /**
//    * @return Some(value) for the first matching value where the supplied function returns true for the given key,
//    * if no matches it returns None
//    */
//  def findValue(key: K)(f: (V) ⇒ Boolean): Option[V] =
//    classifiersToSubscribers get key match {
//      case null ⇒ None
//      case set  ⇒ set.iterator.asScala find f
//    }
//
//  /**
//    * Returns an Iterator of V containing the values for the supplied key, or an empty iterator if the key doesn't exist
//    */
//  def valueIterator(key: K): scala.Iterator[V] = {
//    classifiersToSubscribers.get(key) match {
//      case null ⇒ Iterator.empty
//      case some ⇒ some.iterator.asScala
//    }
//  }
//
//  /**
//    * Applies the supplied function to all keys and their values
//    */
//  def foreach(fun: (K, V) ⇒ Unit): Unit =
//    classifiersToSubscribers.entrySet.iterator.asScala foreach { e ⇒ e.getValue.iterator.asScala.foreach(fun(e.getKey, _)) }
//
//  /**
//    * Returns the union of all value sets.
//    */
//  def values: Set[V] = {
//    val builder = Set.newBuilder[V]
//    for {
//      values ← classifiersToSubscribers.values.iterator.asScala
//      v ← values.iterator.asScala
//    } builder += v
//    builder.result()
//  }
//
//  /**
//    * Returns the key set.
//    */
//  def keys: Iterable[K] = classifiersToSubscribers.keySet.asScala
//
//  /**
//    * Disassociates the value of type V from the key of type K
//    * @return true if the value was disassociated from the key and false if it wasn't previously associated with the key
//    */
//  def remove(key: K, value: V): Boolean = {
//    val set = classifiersToSubscribers get key
//
//    if (set ne null) {
//      set.synchronized {
//        if (set.remove(value)) { //If we can remove the value
//          if (set.isEmpty) //and the set becomes empty
//            classifiersToSubscribers.remove(key, emptySet) //We try to remove the key if it's mapped to an empty set
//
//          true //Remove succeeded
//        } else false //Remove failed
//      }
//    } else false //Remove failed
//  }
//
//  /**
//    * Disassociates all the values for the specified key
//    * @return None if the key wasn't associated at all, or Some(scala.Iterable[V]) if it was associated
//    */
//  def remove(key: K): Option[Iterable[V]] = {
//    val set = classifiersToSubscribers get key
//
//    if (set ne null) {
//      set.synchronized {
//        classifiersToSubscribers.remove(key, set)
//        val ret = collectionAsScalaIterableConverter(set.clone()).asScala // Make copy since we need to clear the original
//        set.clear() // Clear the original set to signal to any pending writers that there was a conflict
//        Some(ret)
//      }
//    } else None //Remove failed
//  }
//
//  /**
//    * Removes the specified value from all keys
//    */
//  def removeValue(value: V): Unit = {
//    val i = classifiersToSubscribers.entrySet().iterator()
//    while (i.hasNext) {
//      val e = i.next()
//      val set = e.getValue()
//
//      if (set ne null) {
//        set.synchronized {
//          if (set.remove(value)) { //If we can remove the value
//            if (set.isEmpty) //and the set becomes empty
//              classifiersToSubscribers.remove(e.getKey, emptySet) //We try to remove the key if it's mapped to an empty set
//          }
//        }
//      }
//    }
//  }
//
//  /**
//    * @return true if the underlying containers is empty, may report false negatives when the last remove is underway
//    */
//  def isEmpty: Boolean = classifiersToSubscribers.isEmpty
//
//  /**
//    *  Removes all keys and all values
//    */
//  def clear(): Unit = {
//    val i = classifiersToSubscribers.entrySet().iterator()
//    while (i.hasNext) {
//      val e = i.next()
//      val set = e.getValue()
//      if (set ne null) { set.synchronized { set.clear(); classifiersToSubscribers.remove(e.getKey, emptySet) } }
//    }
//  }
//}