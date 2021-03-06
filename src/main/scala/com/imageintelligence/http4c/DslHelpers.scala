package com.imageintelligence.http4c

import org.http4s._
import org.http4s.dsl._

import scalaz._
import scalaz.Scalaz._

object DslHelpers {

  case class MatchPathVar[A](f: String => Option[A]) {
    def unapply(str: String): Option[A] = f(str)
  }

  def pathMatcher[A](f: String => Option[A]): MatchPathVar[A] = {
    MatchPathVar(f)
  }

  def validatingPathMatcher[E, A](f: String => E \/ A): MatchPathVar[E \/ A] = {
    val v: String => Option[E \/ A] = f(_) match {
      case \/-(a)   => Some(a.right)
      case -\/(e) => Some(e.left)
    }
    MatchPathVar(v)
  }

  def queryParamDecoderFromEither[A](f: String => ParseFailure \/ A): QueryParamDecoder[A] = {
    new QueryParamDecoder[A] {
      def decode(value: QueryParameterValue) = {
        Validation.fromEither(f(value.value).toEither).toValidationNel
      }
    }
  }

  def optionalQueryParam[A: QueryParamDecoder](name: String): OptionalQueryParamDecoderMatcher[A] = {
    object QueryParam extends OptionalQueryParamDecoderMatcher[A](name)
    QueryParam
  }

  def requiredQueryParam[A: QueryParamDecoder](name: String): QueryParamDecoderMatcher[A] = {
    object QueryParam extends QueryParamDecoderMatcher[A](name)
    QueryParam
  }

}