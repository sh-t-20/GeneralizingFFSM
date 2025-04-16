# Generalizing Featured Finite State Machines Constructed by Model Merging
In this repository, the implementation of the experiments of the paper "Generalizing Featured Finite State Machines Constructed by Model Merging" are briefly explained.
In these expariments, sampling es perfirmed using $T$-wise method [1] and the Chvatal algorithm [2], using FeatureIDE [3].

## Merging FSMs and Constructing an FFSM

To construct an FFSM, the FSMs of products in a sample should be iteratively merged with each other. This step is done using the ``MergeFSMs`` class in the ``fml`` package.
In this class, the implementation of the FFSM<sub>Diff</sub> algorithm provided in [4,5] is used for constructing the FFSMs.
This class should be run using the following parameters.

* -fm: The "xml" file of the feature model
* -dir0: The directory containing the input FFSMs
* -dir1: The directory containing the FSMs of products in the sample
* -dir2: The directory for storing the files of the constructed FFSMs. In this experiment, the same directory is used for the ``dir0`` and ``dir2`` parameters. The reason is that after merging each FSM into the existing FFSM, an FFSM is constructed and stored in this directory. In the next model merging step, this FFSM is used as an input and is merged with another FSM. The last stored FFSM file corresponds to the final FFSM (i.e., the non-generalized FFSM constructed using this sample).

The type of the similarity metric is determined using the ``algorithm_version`` integer variable as follows.
* 1: The global similarity metric.
* 9: The local similarity metric
* 12: The BFS-based similarity metric

An implementation of the global similarity metric [6] was provided in [4,5], and an implementation of the local similarity metric [6] was provided in [7] which we used for constructing the FFSM models.
The implementation of the BFS-based similarity metric is provided in the current paper.
In this experiment, the order of merging the FSMs should be specified.
The product FSMs are sorted in ascending order of their number of state.
According to this merging order, the indexes of the sample products are stored in the ``product_order`` array.

The parameters $t$, $r$, and $k$ of the FFSM<sub>Diff</sub> algorithm are specified using the variable ``T_value``, ``R_value``, and ``K_value``, respectively.
In these experiments $t=0.5$, $r=1.4$, and $k=0.5$ (based on the results of [6]).

## Generalizing an FFSM

The FFSMs constructed by model merging are generalized using the ``GeneralizeFFSM`` class in the ``fml`` package, which should be run using the following parameters.

* -fm: The "xml" file of the feature model
* -ffsm: The file of the FFSM to be generalized (in "txt" format)
* -out: The output directory for storing the generalized FFSM
* -alphabet: The alphabet of the features (the alphabet of the features of each subject systems is provided in its corresponding directory in the ``experiments`` folder.)
* -no\_loop: If this parameter is set to $\mathit{true}$, the self-loops will be removed from the generalized FFSM. In this way, the resulting FFSM will be smaller in size and easier to evaluate.

The type of the generalization method is determined using the ``generalization_method`` integer variable as follows.

* 1: The basic generalization method
* 3: The lookahead generalization method

## Projecting an FFSM onto a Product Configuration

To evaluate the presented generalization methods in terms of accuracy, the generalized FFSM is projected onto the configurations of the out-of-sample products, and the resulting FSMs are compared with the FSMs of those products.
The generalized FFSM is projected onto a product configuration using the ``ProjectionFFSM`` class in the ``fml2`` package, which should be run using the following parameters.

* -fm: The "xml" file of the feature model
* -ffsm: The generalized FFSM (in "txt" format)
* -out: The output directory for storing the resulting FSM
* -alphabet: The alphabet of the features
* -no\_loop: If this parameter is set to $\mathit{true}$, the self-loops will be removed from the resulting FSM.
* -check\_scc: If this parameter is set to $\mathit{true}$, the states from which there is no path to the initial state, are removed from the FSM.

## Comparing the Structures of Two State Machines

To compare the structure of two state machines, the ``CompareStructure`` class in the ``fml4`` package should be run using the following parameters.

* -fm: The "xml" file of the feature model
* -nfsm: The "txt" file of an NFSM (e.g., the NFSM resulting from projecting an FFSM onto a product configuration)
* -dfsm: The "txt" file of a deterministic FSM (e.g., the product FSM)
* -no\_loop: If this parameter is set to $\mathit{true}$, the self-loops will be removed from the state machines.

In this class, the structures of two state machines are compared using the FFSM<sub>Diff</sub> algorithm, and the Precision, Recall, and F1 are reported.

## Evaluating a generalized FFSM

To evaluate a generalized FFSM, this FFSM is projected onto the configurations of the out-of-sample products which is converted to a DFSM. Then, the resulting FSMs are compared with the FSMs of the corresponding products (structural comparison). To carry out these steps, the ``EvaluateGeneralizedFFSM`` class in ``fml`` package should be run using the following parameters.

* -fm: The "xml" file of the feature model
* -ffsm: The generalized FFSM (in "txt" format)
* -dir: A directory containing the configurations and the FSMs of out-of-sample products
* -out: A directory for storing the FSMs resulting from the projection.
* -alphabet: The alphabet of the features
* -no\_loop: If this parameter is set to $\mathit{true}$, the self-loops will be removed from the FSMs.

The "csv" files containing the results of these experiments are available in the ``results`` directory (in the directory of each subject system).

## Minimizing an FFSM

To minimize an FFSM, the ``MinimizeFFSM`` class in the ``fml6``} package should be run with the following parameters.

* -fm: The "xml" file of the feature model
* -ffsm: The FFSM to be minimized (in "txt" format)
* -out: A directory for storing the minimized FFSM
* -alphabet: The alphabet of the features

## Licensing:

The artifacts are all available under GNU public License 3.0.
These artifacts make use of the following repositories, that are also available under the same license, and are properly attributed in the artifacts:

https://github.com/sh-t-20/artifacts [8]

https://github.com/sh-t-20/EfficientFFSMConstruction [7]

https://github.com/damascenodiego/learningFFSM [4,5]

https://github.com/damascenodiego/DynamicLstarM [9]

## References

[1] M. F. Johansen, Ø. Haugen, and F. Fleurey, "Properties of realistic feature models make combinatorial testing of product lines feasible", in Model Driven Engineering Languages and Systems, 14th International Conference, MODELS 2011, Proceedings, ser. Lecture Notes in Computer Science, vol. 6981. Springer, 2011, pp. 638-652.

[2] V. Chvatal, "A greedy heuristic for the set-covering problem", Mathematics of Operations Research, vol. 4, no. 3, pp. 233-235, 1979.

[3] T. Thum, C. Kastner, F. Benduhn, J. Meinicke, G. Saake, and T. Leich, "FeatureIDE: An extensible framework for feature-oriented software development", Science of Computer Programming, vol. 79, pp. 70-85, 2014.

[4] C. D. N. Damasceno, M. R. Mousavi, and A. da Silva Simao, "Learning by sampling: learning behavioral family models from software product lines", Empirical Software Engineering, vol. 26, no. 1, p. 4, 2021.

[5] C. D. N. Damasceno, M. R. Mousavi, and A. da Silva Simao, "Learning from difference: an automated approach for learning family models from software product lines", in proceedings of the 23rd International Systems and Software Product Line Conference, SPLC 2019, Volume A. ACM, 2019, pp. 10:1-10:12.

[6] N. Walkinshaw, and K. Bogdanov, "Automated comparison of state-based software models in terms of their language and structure", ACM TOSEM, vol. 22, no. 2, pp. 13:1-13:37, 2013.

[7] S. Tavassoli, and R. Khosravi, "Efficient Construction of family-Based behavioral models from adaptively learned models", Software and Systems Modeling, vol. 24, no. 1, pp. 225-251, 2025.

[8] S. Tavassoli, C. D. N. Damasceno, R. Khosravi, and M. R. Mousavi, "Adaptive behavioral model learning for software product lines", in SPLC'22: 26th ACM International Systems and Software Product Line Conference, Volume A. ACM, 2022, pp. 142-153.

[9] C. D. N. Damasceno, M. R. Mousavi, and A. da Silva Simao, "Learning to reuse: Adaptive model learning for evolving systems", in Integrated Formal Methods - 15th International Conference, IFM 2019, Proceedings, ser. Lecture Notes in Computer Science, vol. 11918. Springer, 2019, pp. 138-156.
