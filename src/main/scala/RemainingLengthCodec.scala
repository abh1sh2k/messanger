package net.sigusr.mqtt.impl.frames

import scodec.Attempt._
import scodec._
import scodec.bits.{BitVector, _}
import scodec.codecs._

final class RemainingLengthCodec extends Codec[Int] {

  val MinValue = 0
  val MaxValue = 268435455

  def sizeBound = SizeBound.bounded(8, 32)

  def decode(bits: BitVector): Attempt[DecodeResult[Int]] = {
    @annotation.tailrec
    def decodeAux(step: Attempt[DecodeResult[Int]], factor: Int, depth: Int, value: Int): Attempt[DecodeResult[Int]] =
      if (depth == 4) failure(Err("The remaining length must be 4 bytes long at most"))
      else step match {
        case f: Failure ⇒ f
        case Successful(d) ⇒
          if ((d.value & 128) == 0) successful(DecodeResult(value + (d.value & 127) * factor, d.remainder))
          else decodeAux(uint8.decode(d.remainder), factor * 128, depth + 1, value + (d.value & 127) * factor)
      }
    decodeAux(uint8.decode(bits), 1, 0, 0)
  }

  def encode(value: Int): Attempt[BitVector] = {
    @annotation.tailrec
    def encodeAux(value: Int, digit: Int, bytes: ByteVector): ByteVector =
      if (value == 0) bytes :+ digit.asInstanceOf[Byte]
      else encodeAux(value / 128, value % 128, bytes :+ (digit | 0x80).asInstanceOf[Byte])
    if (value < MinValue || value > MaxValue) failure(Err(s"The remaining length must be in the range [$MinValue..$MaxValue], $value is not valid"))
    else successful(BitVector(encodeAux(value / 128, value % 128, ByteVector.empty)))
  }
}