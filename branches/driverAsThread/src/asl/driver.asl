// Agent boss in project delegate-tasks

/* Initial beliefs and rules */

/*rules*/

has_od(S,G) :- 
	start(S) & goal(G) &
	S > 0 & G > 0.

/*beliefs*/
status(parked). /*[parked,start_engine,driving]*/
start(0).
goal(0).

/* Initial goals */

/* Plans */

/* itsumo solicita uma ação ao motorista. 
se o motorista quer ir a algum lugar 
(has_od) e está estacionado, então ele 
inicia sua viagem (drive) */
@ds1
+decide(something): status(parked) & has_od(S,G)
	<- 	-+status(start_engine); /* update belief base */
		//.concat("trigger: requested action; context: status(parked) & has_od(S,G)", M);
		//.print(M);
		!drive(S,G).
		
/* itsumo solicita uma ação ao motorista. 
se o motorista já está em movimento então 
envia uma mensagem informando que não 
há nada novo para fazer */
@ds2		
+decide(something): not status(parked) 
	<- jia.doNothing.

@dr1
+!drive(S,G): status(start_engine)
	<-  -+status(driving);
		.concat("driving from ", S ," to ", G , M);
		.print(M);
		jia.driveOnPath(S,G). /* envia mensagem para o simulador 
			movimentar o veículo de S até G */
	
@dr2
+!drive(S,G): status(driving)
	<-	jia.doNothing.
	
@ah
//+broken(car): status(driving) & has_od(S,G)
+broken(car): has_od(S,G)
	 <- -broken(car);
	 	.concat("my car is broken! need help @ node ", S ,"!", M);
		.print(M);
	 	.send(help0, achieve, goHelp(S)).

+!fixed: true
	<- 	-+decide(something); 
		-+status(parked).
	 	
//finish trip
+status(finish): true
	<-	.concat("arrived!", M);
		.print(M).