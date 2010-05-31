// Agent boss in project delegate-tasks

/* Initial beliefs and rules */

/*rules*/

has_od(S,G) :- 
	start(S) & goal(G) &
	S > 0 & G > 0.

an_agent(Ag) :-
   .all_names(L) & .length(L,X) & .random(R,0) &
   .nth(R*X,L,Ag) & .substring("driver", Ag). /*1+ = evitar agente zero*/

/*beliefs*/
status(parked). /*[parked,start_engine,driving]*/
/*decide(nothing).*/ /*[nothing,something]*/
act(sleep).

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
		.concat("trigger: requested action; context: status(parked) & has_od(S,G)", M);
		.print(M);
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
		-+act(help);
		.concat("driving from ", S ," to ", G , M);
		.print(M);
		jia.driveOnPath(S,G). /* envia mensagem para o simulador 
			movimentar o veículo de S até G */
	
@dr2
+!drive(S,G): status(driving)
	<-	jia.doNothing.
	
+!goHelp(driver,G): status(parked)
	<-	-+decide(something);
		-+goal(G).
		
+!doHelp(driver): status(parked) & act(help)
	<-	.concat("car is ok!", M);
		.send(driver,tell,msg(M)).