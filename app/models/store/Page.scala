package models.store

import scala.collection.GenTraversableOnce

case class Pageable(pageNumber: Int = 1, pageSize: Int = 2) {
  def offset = pageSize * (pageNumber - 1)
}

case class Page[A](pageable: Pageable, total: Int, seq: A*) {
  def foreach[B](f: A => B): Unit = seq.toTraversable.foreach(f)

  def flatMap[B](f: A => GenTraversableOnce[B]) = seq.toTraversable.flatMap(f)
  
  def map[B](f: A => B) = seq.toTraversable.map(f)
  
  def filter(p: A => Boolean) = seq.toTraversable.filter(p)
  
  def zipWithIndex = seq.toSeq.zipWithIndex

  def hasNextPage = pageable.offset + pageable.pageSize < total

  def hasPreviousPage = pageable.offset > pageable.pageSize
  
  def totalPages = total % pageable.pageSize match {
    case 0 => total / pageable.pageSize
    case _ => total / pageable.pageSize + 1
  }
  
  def currentPageNumber = pageable.pageNumber
}
