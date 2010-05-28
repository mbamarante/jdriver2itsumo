// Agent boss in project delegate-tasks

/* Initial beliefs and rules */

/*rules*/

has_od :- 
	start(S) &
	goal(G) &
	S > 0 & 
	G > 0.

an_agent(Ag) :-
   .all_names(L) & .length(L,X) & .random(R,0) &
   .nth(R*X,L,Ag) & .substring("driver", Ag). /*1+ = evitar agente zero*/

/*beliefs*/

driving(false).
start(0).
goal(0).

/* Initial goals */

!start_engine.

/* Plans */

@d1
+!start_engine : driving(false) & has_od & start(S) & goal(G)
	<-  .concat("not driving and [O,D] is [",S,",",G,"] -- drive!", M);
		.print(M);
		!drive(O,D).
	
+!drive(O,D) : driving(false)
	<-  jia.driveOnPath(O,D).

/*@c1
+!connect[source(D)]
	: true <-
	.print(D);
	jia.proxyConnectDriver(D).
*/