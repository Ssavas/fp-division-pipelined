// Süleyman Savas, Halmstad University
// 2017-02-13


/*
	If the inputs are given in the first cycle
	the result comes out at fifth cycle
*/

package cintacc

import chisel3._
import chisel3.util._
//import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class fpDiv(val w : Int) extends Module{ //w = 32
	val io = IO(new Bundle{
		val in1  = Input(UInt(width = w))	
		val in2  = Input(UInt(width = w))
		val out  = Output(UInt(width = w))
	})

	val inverter   = Module(new fpInverter(23))
	val multiplier = Module(new FPMult(w))

	inverter.io.in1 := io.in2(22, 0)	//mantissa 
	// delay input1 due to the inverter
	val in1Reg0 = Reg(init = 0.U, next = io.in1)	// stage 0
	val in1Reg1 = Reg(init = 0.U, next = in1Reg0)	// stage 1
	val in1Reg2 = Reg(init = 0.U, next = in1Reg1)	// stage 2
	val in1Reg3 = Reg(init = 0.U, next = in1Reg2)	// stage 3

	// delay input2 exponent and sign due to the inverter
	val in2ExpReg0  = Reg(init = 0.U, next = io.in2(30,23))	// stage 0
	val in2SignReg0 = Reg(init = 0.U, next = io.in2(31))	// stage 0
	val in2ExpReg1  = Reg(init = 0.U, next = in2ExpReg0)	// stage 1
	val in2SignReg1 = Reg(init = 0.U, next = in2SignReg0)	// stage 1
	val in2ExpReg2  = Reg(init = 0.U, next = in2ExpReg1)	// stage 2
	val in2SignReg2 = Reg(init = 0.U, next = in2SignReg1)	// stage 2
	val in2ExpReg3  = Reg(init = 0.U, next = in2ExpReg2)	// stage 3
	val in2SignReg3 = Reg(init = 0.U, next = in2SignReg2)	// stage 3
	val invMantReg  = Reg(init = 0.U, next = inverter.io.out(23, 0)) //stage 3

	val invMant       = invMantReg
	val negExpTmp     = 254.U - in2ExpReg3
	val negExp        = Mux(invMant === 0.U, negExpTmp, negExpTmp - 1.U)

	// we should raise an execption if both mantissa and exponent are zero (the final result should be inf

	multiplier.io.a := in1Reg3
	multiplier.io.b := Cat(in2SignReg3, negExp, invMant(23, 1))
	// skipping msb of inverter (multiplying mantissa by 2)
	
	io.out := multiplier.io.res
/*	
	printf("\nin1: %d, in2: %d\n", io.in1, io.in2)
	printf("inverter in   : %d out: %d\n", io.in2(22, 0), inverter.io.out)
	printf("invmant       : %d \n", invMant)
	printf("multiplier a  : %d\n", in1Reg3)
	printf("multiplier b  : %d\n", Cat(in2SignReg3, negExp, inverter.io.out(23,1)))
	//printf("exponent2: %d\n", exponent2)
	printf("negExp        : %d, in1 exp %d\n", negExp, io.in1(30, 23))
	printf("multiplier out: %d\n", multiplier.io.res)
	*/
//	printf("%d\n", multiplier.io.res)

}
/*
class fpDivTest(c: fpDiv) extends PeekPokeTester(c) {
/*
	poke(c.io.in1, 1096155136.U)		// 13.75
	poke(c.io.in2, 1091829760.U)		// 9.25

	step(1)
	expect(c.io.out, 1123512320.U)		//123.1875
	step(1)
*/

	poke(c.io.in1, 1065353216.U)	//65536
	poke(c.io.in2, 0.U)	//0
	step(1)
	expect(c.io.out, 0.U)

	poke(c.io.in1, 2139095039.U)
	poke(c.io.in2, 2139095039.U)	
	
	step(1)
	expect(c.io.out, 0.U)

	poke(c.io.in1, 1199570944.U)	//65536
	poke(c.io.in2, 1106771968.U)	//31
	step(1)
	expect(c.io.out, 0.U)

	//poke(c.io.in2, 1098383360.U) // 15.5
	poke(c.io.in1, 2139095039.U)	// biggest single precision float
	poke(c.io.in2, 1207435264.U)
	step(1)
	expect(c.io.out, 0.U)

	poke(c.io.in1, 2139095039.U)
	poke(c.io.in2, 1200035266.U)	
	
	step(1)
	expect(c.io.out, 0.U)

	poke(c.io.in1, 1082130431.U)	// exp = 1, mantissa = 28 ones
	poke(c.io.in2, 1106771968.U)	// 31

	step(1)
	expect(c.io.out, 0.U)
	poke(c.io.in1, 1207435265.U)
	poke(c.io.in2, 2139095039.U)
	step(1)
	expect(c.io.out, 0.U)
	step(1)
	expect(c.io.out, 0.U)
	poke(c.io.in1, 1207959552.U)
//	poke(c.io.in1, 1200035266.U)
	step(1)
	expect(c.io.out, 0.U)

	poke(c.io.in1, 0.U)
	poke(c.io.in2, 0.U)
	step(1)
	expect(c.io.out, 0.U)
	step(1)
	expect(c.io.out, 0.U)
	step(1)
	expect(c.io.out, 0.U)
	step(1)
	expect(c.io.out, 0.U)

	//poke(c.io.in2, 0.U)

// 1125056512 = 143.0

// 1199570944 = 65536
// 1207959552 = 131072
// 1106771968 = 31
/*
	for (i <- 1199570944 until 1207959552){
//	for (i <- 1199570944 until 1200095232){
		poke(c.io.in1, i)
		poke(c.io.in2, 1106771968.U)
		step(1)

	}
*/

/*
	expect(c.io.out, 0.U)
	step(1)
	expect(c.io.out, 0.U)
	step(1)
	expect(c.io.out, 0.U)
	step(1)
	expect(c.io.out, 0.U)
	step(1)
	expect(c.io.out, 0.U)
	step(1)
	expect(c.io.out, 0.U)
*/
}

object fpDiv {
  def main(args: Array[String]): Unit = {
    if (!Driver(() => new fpDiv(32))(c => new fpDivTest(c))) System.exit(1)
  }
}


*/








