package edu.albany.cs.fastPCST;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * fast-PCST Algorithm. We would like to say thank you to Dr. Ludwig Schmidt. He
 * provided his c++ code to us (https://github.com/ludwigschmidt/pcst-fast).
 * This algorithm has been published in the following paper:
 * 
 * title : A Fast, Adaptive Variant of the Goemans-Williamson Scheme for the
 * Prize-Collecting Steiner Tree Problem
 * 
 * url: http://people.csail.mit.edu/ludwigs/papers/dimacs14_fastpcst.pdf
 * 
 * Please cite his paper and our paper, when you use this java-version code.
 * 
 * @author baojian bzhou6@albany.edu
 *
 */
public class FastPCST {

	// defined parameters for fast pcst
	private ArrayList<PairingHeap.Node> pairing_heap_buffer = new ArrayList<PairingHeap.Node>(); // public
																									// heap_buffer
																									// (maybe
																									// do
																									// not
																									// need
																									// this)
	private ArrayList<EdgePart> edge_parts = new ArrayList<EdgePart>(); // edge
																		// parts
																		// information
	private ArrayList<EdgeInfo> edge_info = new ArrayList<EdgeInfo>();
	private ArrayList<Cluster> clusters = new ArrayList<Cluster>();
	private ArrayList<InactiveMergeEvent> inactive_merge_events = new ArrayList<InactiveMergeEvent>();
	private PriorityQueue clusters_deactivation = new PriorityQueue();
	private PriorityQueue clusters_next_edge_event = new PriorityQueue();
	private double current_time;
	private double eps;
	private ArrayList<Boolean> node_good = new ArrayList<Boolean>(); // marks
																		// whether
																		// a
																		// node
																		// survives
																		// simple
																		// pruning
	private ArrayList<Boolean> node_deleted = new ArrayList<Boolean>();
	private ArrayList<Integer> phase2_result = new ArrayList<Integer>();
	private ArrayList<Pair<Integer, Double>> path_compression_visited = new ArrayList<Pair<Integer, Double>>();
	private ArrayList<Integer> cluster_queue = new ArrayList<Integer>();
	private ArrayList<ArrayList<Pair<Integer, Double>>> phase3_neighbors = new ArrayList<ArrayList<Pair<Integer, Double>>>();

	// for strong pruning
	private ArrayList<Integer> final_component_label = new ArrayList<Integer>();
	private ArrayList<ArrayList<Integer>> final_components = new ArrayList<ArrayList<Integer>>();
	private int root_component_index;
	private ArrayList<Pair<Integer, Double>> strong_pruning_parent = new ArrayList<Pair<Integer, Double>>();
	private ArrayList<Double> strong_pruning_payoff = new ArrayList<Double>();
	private ArrayList<Pair<Boolean, Integer>> stack = new ArrayList<Pair<Boolean, Integer>>();
	private ArrayList<Integer> stack2 = new ArrayList<Integer>();

	// used in the input of constructor
	public static final int kNoRoot = -1;
	private ArrayList<Integer[]> edges;
	private ArrayList<Double> costs;
	private ArrayList<Double> prizes;
	private int root;
	private int target_num_active_clusters;
	private PruningMethod pruning;
	private int verbosity_level;

	// results of algorithm
	public FastPCST.Statistics stats = new FastPCST.Statistics();
	public ArrayList<Integer> result_nodes;
	public ArrayList<Integer> result_edges;

	// reference parameters
	private double next_edge_time = 0.0d;
	private int next_edge_cluster_index = 0;
	private int next_edge_part_index = -1;
	private double next_cluster_time;
	private int next_cluster_index;
	private double sum_current_edge_part;
	private double current_finished_moat_sum;
	private int current_cluster_index;
	private double sum_other_edge_part;
	private int other_cluster_index;
	private double other_finished_moat_sum;
	private Cluster current_cluster;
	private Cluster other_cluster;
	private EdgePart next_edge_part;
	private EdgePart other_edge_part;
	private ArrayList<Integer> phase1_result;

	// reference parameters
	private ArrayList<Integer> build_phase1_node_set_second_P;
	private ArrayList<Integer> build_phase1_node_set_first_P;
	private ArrayList<Integer> build_phase2_node_set_first_P;
	private ArrayList<Integer> build_phase3_node_set_first_P;

	public FastPCST(ArrayList<Integer[]> edges, ArrayList<Double> prizes, ArrayList<Double> costs,
			int target_num_active_clusters) {
		this(edges, prizes, costs, FastPCST.kNoRoot, target_num_active_clusters, FastPCST.PruningMethod.kStrongPruning,
				-1);
	}

	/**
	 * @param edges
	 * @param prizes
	 * @param costs
	 * @param root
	 * @param target_num_active_clusters
	 * @param pruningMethod
	 * @param verbostiy_level
	 */
	public FastPCST(ArrayList<Integer[]> edges, ArrayList<Double> prizes, ArrayList<Double> costs, int root,
			int target_num_active_clusters, PruningMethod pruningMethod, int verbostiy_level) {

		this.edges = edges;
		this.prizes = prizes;
		this.costs = costs;
		this.root = root;
		this.target_num_active_clusters = target_num_active_clusters;
		this.pruning = pruningMethod;
		this.verbosity_level = verbostiy_level;

		if (!checkValidInput()) {
			System.out.println("Error : input parameters are not valid..");
			System.exit(0);
		}

		edge_parts = new ArrayList<EdgePart>();
		edge_parts = Utils.resize(edge_parts, 2 * this.edges.size(), new EdgePart());
		node_deleted = new ArrayList<Boolean>();
		node_deleted = Utils.resize(node_deleted, prizes.size(), false);
		edge_info = new ArrayList<EdgeInfo>();
		edge_info = Utils.resize(edge_info, this.edges.size(), new EdgeInfo());

		for (int ii = 0; ii < edge_info.size(); ++ii) {
			edge_info.get(ii).inactiveMergeEvent = -1;
		}
		current_time = 0.0;
		eps = 1e-10; // set to min input value / 2.0?
		// initialize clusters clusters_deactivation
		for (int ii = 0; ii < this.prizes.size(); ++ii) {
			Cluster cluster = new Cluster(pairing_heap_buffer);
			clusters.add(cluster);
			clusters.get(ii).active = (ii != root);
			clusters.get(ii).active_start_time = 0.0;
			clusters.get(ii).active_end_time = -1.0;
			if (ii == root) {
				clusters.get(ii).active_end_time = 0.0;
			}
			clusters.get(ii).merged_into = -1;
			clusters.get(ii).prize_sum = prizes.get(ii);
			clusters.get(ii).subcluster_moat_sum = 0.0;
			clusters.get(ii).moat = 0.0;
			clusters.get(ii).contains_root = (ii == root);
			clusters.get(ii).skip_up = -1;
			clusters.get(ii).skip_up_sum = 0.0;
			clusters.get(ii).merged_along = -1;
			clusters.get(ii).child_cluster_1 = -1;
			clusters.get(ii).child_cluster_2 = -1;
			clusters.get(ii).necessary = false;
			if (clusters.get(ii).active) {
				clusters_deactivation.insert(prizes.get(ii), ii);
			}
		}
		// split edge into two parts
		for (int ii = 0; ii < this.edges.size(); ++ii) {
			int uu = this.edges.get(ii)[0];
			int vv = this.edges.get(ii)[1];
			double cost = costs.get(ii);
			EdgePart uu_part = edge_parts.get(2 * ii);
			EdgePart vv_part = edge_parts.get(2 * ii + 1);
			Cluster uu_cluster = clusters.get(uu);
			Cluster vv_cluster = clusters.get(vv);
			uu_part.deleted = false;
			vv_part.deleted = false;
			if (uu_cluster.active && vv_cluster.active) {
				double event_time = cost / 2.0d;
				uu_part.nextEventVal = event_time;
				vv_part.nextEventVal = event_time;
			} else if (uu_cluster.active) {
				uu_part.nextEventVal = cost;
				vv_part.nextEventVal = 0.0;
			} else if (vv_cluster.active) {
				uu_part.nextEventVal = 0.0;
				vv_part.nextEventVal = cost;
			} else {
				uu_part.nextEventVal = 0.0;
				vv_part.nextEventVal = 0.0;
			}
			uu_part.heapNode = uu_cluster.edge_parts.insert(uu_part.nextEventVal, 2 * ii);
			vv_part.heapNode = vv_cluster.edge_parts.insert(vv_part.nextEventVal, (2 * ii + 1));
			clusters.set(uu, uu_cluster);
			clusters.set(vv, vv_cluster);
			edge_parts.set(2 * ii, uu_part);
			edge_parts.set(2 * ii + 1, vv_part);
		}

		// initialize clusters_next_edge_event
		for (int ii = 0; ii < prizes.size(); ++ii) {
			if (clusters.get(ii).active) {
				if (!clusters.get(ii).edge_parts.is_empty()) {
					double val = 0.0;
					int edge_part = -1;
					clusters.get(ii).edge_parts.get_min(val, edge_part);
					val = clusters.get(ii).edge_parts.getMinFirstP;
					edge_part = clusters.get(ii).edge_parts.getMinSecondP;
					clusters_next_edge_event.insert(val, ii);
				}
			}
		}
	}// constructor

	private boolean checkValidInput() {
		for (Double prize : this.prizes) {
			if (prize < 0.0D) {
				System.out.println("Error : input parameter prizes are not valid..");
				return false;
			}
		}
		for (Double cost : this.costs) {
			if (cost < 0.0D) {
				System.out.println("Error : input parameter costs are not valid..");
				return false;
			}
		}
		return true;
	}

	private int get_other_edge_part_index(int edge_part_index) {
		if (edge_part_index % 2 == 0) {
			return (edge_part_index + 1);
		} else {
			return (edge_part_index - 1);
		}
	}

	public FastPCST.PruningMethod parse_pruning_method(String input) {
		PruningMethod result = PruningMethod.kUnknownPruning;
		String input_lower = input.toLowerCase();
		input = input_lower;
		if (input.equals("none")) {
			result = PruningMethod.kNoPruning;
		} else if (input.equals("simple")) {
			result = PruningMethod.kSimplePruning;
		} else if (input.equals("gw")) {
			result = PruningMethod.kGWPruning;
		} else if (input.equals("strong")) {
			result = PruningMethod.kStrongPruning;
		}
		return result;
	}

	public FastPCST.Statistics get_statistics() {
		return this.stats;
	}

	public void get_next_edge_event(Double next_time, Integer next_cluster_index, Integer next_edge_part_index) {
		if (clusters_next_edge_event.isEmpty()) {
			next_time = Double.POSITIVE_INFINITY;
			next_cluster_index = -1;
			next_edge_part_index = -1;
			this.next_edge_time = next_time;
			this.next_edge_cluster_index = next_cluster_index;
			this.next_edge_part_index = next_edge_part_index;
		}
		clusters_next_edge_event.getMin(next_time, next_cluster_index);
		next_time = clusters_next_edge_event.getMinFirstP;
		next_cluster_index = clusters_next_edge_event.getMinSecondP;
		clusters.get(next_cluster_index).edge_parts.get_min(next_time, next_edge_part_index);
		next_time = clusters.get(next_cluster_index).edge_parts.getMinFirstP;
		next_edge_part_index = clusters.get(next_cluster_index).edge_parts.getMinSecondP;
		this.next_edge_time = next_time;
		this.next_edge_cluster_index = next_cluster_index;
		this.next_edge_part_index = next_edge_part_index;
	}

	public void remove_next_edge_event(int next_cluster_index) {
		clusters_next_edge_event.deleteElement(next_cluster_index);
		double tmp_value = 0d;
		int tmp_edge_part = 1;
		clusters.get(next_cluster_index).edge_parts.deleteMin(tmp_value, tmp_edge_part);
		tmp_value = clusters.get(next_cluster_index).edge_parts.deleteMinFirstP;
		tmp_edge_part = clusters.get(next_cluster_index).edge_parts.deleteMinSecondP;
		if (!clusters.get(next_cluster_index).edge_parts.is_empty()) {
			clusters.get(next_cluster_index).edge_parts.get_min(tmp_value, tmp_edge_part);
			tmp_value = clusters.get(next_cluster_index).edge_parts.getMinFirstP;
			tmp_edge_part = clusters.get(next_cluster_index).edge_parts.getMinSecondP;
			clusters_next_edge_event.insert(tmp_value, next_cluster_index);
		}
	}

	public void get_next_cluster_event(Double next_cluster_time, Integer next_cluster_index) {
		if (clusters_deactivation.isEmpty()) {
			next_cluster_time = Double.POSITIVE_INFINITY;
			next_cluster_index = -1;
			this.next_cluster_time = next_cluster_time;
			this.next_cluster_index = next_cluster_index;
			return;
		}
		clusters_deactivation.getMin(next_cluster_time, next_cluster_index);
		next_cluster_time = clusters_deactivation.getMinFirstP;
		next_cluster_index = clusters_deactivation.getMinSecondP;
		this.next_cluster_time = clusters_deactivation.getMinFirstP;
		this.next_cluster_index = clusters_deactivation.getMinSecondP;
	}

	public void remove_next_cluster_event() {
		Double tmp_value = 0d;
		Integer tmp_cluster = 1;
		clusters_deactivation.deleteMin(tmp_value, tmp_cluster);
		tmp_value = clusters_deactivation.deleteMinFirstP;
		tmp_cluster = clusters_deactivation.deleteMinSecondP;
	}

	public void get_sum_on_edge_part(int edge_part_index, double total_sum, Double finished_moat_sum,
			Integer cur_cluster_index, int flag) {
		int endpoint = edges.get(edge_part_index / 2)[0];
		if (edge_part_index % 2 == 1) {
			endpoint = edges.get(edge_part_index / 2)[1];
		}
		total_sum = 0.0;
		cur_cluster_index = endpoint;
		path_compression_visited = new ArrayList<Pair<Integer, Double>>();
		while (clusters.get(cur_cluster_index).merged_into != -1) {
			path_compression_visited.add(new Pair<Integer, Double>(cur_cluster_index, total_sum));
			if (clusters.get(cur_cluster_index).skip_up >= 0) {
				total_sum += clusters.get(cur_cluster_index).skip_up_sum;
				cur_cluster_index = clusters.get(cur_cluster_index).skip_up;
			} else {
				total_sum += clusters.get(cur_cluster_index).moat;
				cur_cluster_index = clusters.get(cur_cluster_index).merged_into;
			}
		}
		for (int ii = 0; ii < path_compression_visited.size(); ++ii) {
			int visited_cluster_index = path_compression_visited.get(ii).getFirst();
			double visited_sum = path_compression_visited.get(ii).getSecond();
			clusters.get(visited_cluster_index).skip_up = cur_cluster_index;
			clusters.get(visited_cluster_index).skip_up_sum = total_sum - visited_sum;
		}
		if (clusters.get(cur_cluster_index).active) {
			finished_moat_sum = total_sum;
			total_sum += current_time - clusters.get(cur_cluster_index).active_start_time;
		} else {
			total_sum += clusters.get(cur_cluster_index).moat;
			finished_moat_sum = total_sum;
		}
		if (flag == 1) {
			sum_current_edge_part = total_sum;
			current_finished_moat_sum = finished_moat_sum;
			current_cluster_index = cur_cluster_index;
		} else {
			sum_other_edge_part = total_sum;
			other_finished_moat_sum = finished_moat_sum;
			other_cluster_index = cur_cluster_index;
		}
	}

	public void mark_nodes_as_good(int start_cluster_index) {
		cluster_queue = new ArrayList<Integer>();
		int queue_index = 0;
		cluster_queue.add(start_cluster_index);
		while (queue_index < (int) cluster_queue.size()) {
			int cur_cluster_index = cluster_queue.get(queue_index);
			queue_index += 1;
			if (clusters.get(cur_cluster_index).merged_along >= 0) {
				cluster_queue.add(clusters.get(cur_cluster_index).child_cluster_1);
				cluster_queue.add(clusters.get(cur_cluster_index).child_cluster_2);
			} else {
				node_good.set(cur_cluster_index, true);
			}
		}
	}

	public void mark_clusters_as_necessary(int start_cluster_index) {
		int cur_cluster_index = start_cluster_index;
		while (!clusters.get(cur_cluster_index).necessary) {
			clusters.get(cur_cluster_index).necessary = true;
			if (clusters.get(cur_cluster_index).merged_into >= 0) {
				cur_cluster_index = clusters.get(cur_cluster_index).merged_into;
			} else {
				return;
			}
		}
	}

	public void mark_nodes_as_deleted(int start_node_index, int parent_node_index) {
		node_deleted.set(start_node_index, true);
		cluster_queue = new ArrayList<Integer>();
		int queue_index = 0;
		cluster_queue.add(start_node_index);
		while (queue_index < cluster_queue.size()) {
			int cur_node_index = cluster_queue.get(queue_index);
			queue_index += 1;
			for (int ii = 0; ii < phase3_neighbors.get(cur_node_index).size(); ++ii) {
				int next_node_index = phase3_neighbors.get(cur_node_index).get(ii).getFirst();
				if (next_node_index == parent_node_index) {
					continue;
				}
				if (node_deleted.get(next_node_index)) {
					continue; // should never happen
				}
				node_deleted.set(next_node_index, true);
				cluster_queue.add(next_node_index);
			}
		}
	}

	public boolean run(ArrayList<Integer> result_nodes, ArrayList<Integer> result_edges) {

		result_nodes = new ArrayList<Integer>();
		result_edges = new ArrayList<Integer>();
		if (root >= 0 && target_num_active_clusters > 0) {
			System.out.println("Error: target_num_active_clusters must be 0 in the rooted case.\n");
			System.exit(0);
			return false;
		}
		phase1_result = new ArrayList<Integer>();
		int num_active_clusters = prizes.size();
		if (root >= 0) {
			num_active_clusters -= 1;
		}
		// growth phase
		while (num_active_clusters > target_num_active_clusters) {
			if (verbosity_level >= 2) {
				System.out.print("-----------------------------------------\n");
			}
			next_edge_time = 0.0;
			next_edge_cluster_index = 0;
			next_edge_part_index = -1;
			get_next_edge_event(next_edge_time, next_edge_cluster_index, next_edge_part_index);
			get_next_cluster_event(next_cluster_time, next_cluster_index);
			if (verbosity_level >= 2) {
				System.out.format("Next edge event: time %6f, cluster %d, part %d\n", next_edge_time,
						next_edge_cluster_index, next_edge_part_index);
				System.out.format("Next cluster event: time %6f, cluster %d\n", next_cluster_time, next_cluster_index);
			}
			// edge is tight
			if (next_edge_time < next_cluster_time) {
				if (verbosity_level >= 3) {
					System.out.format("next_edge_time : %6f ; next_cluster_time : %6f\n", next_edge_time,
							next_cluster_time);
				}
				stats.total_num_edge_events += 1;
				current_time = next_edge_time;
				remove_next_edge_event(next_edge_cluster_index);
				if (edge_parts.get(next_edge_part_index).deleted) {
					stats.num_deleted_edge_events += 1;
					if (verbosity_level >= 2) {
						System.out.format("Edge part %d already deleted, nothing to do\n", next_edge_part_index);
					}
					// System.out.println("PCSF Test F:" +
					// stats.total_num_edge_events) ;
					continue;
				}
				// collect all the relevant information about the edge parts
				int other_edge_part_index = get_other_edge_part_index(next_edge_part_index);
				double current_edge_cost = costs.get(next_edge_part_index / 2);
				sum_current_edge_part = 0.0d;
				current_cluster_index = -1;
				current_finished_moat_sum = -1;
				get_sum_on_edge_part(next_edge_part_index, sum_current_edge_part, current_finished_moat_sum,
						current_cluster_index, 1);
				if (verbosity_level >= 3) {
					System.out.format(
							"next_edge_part_index : %d ; sum_current_edge_part : %6f ; current_finished_moat_sum : %6f ; current_cluster_index : %d\n",
							next_edge_part_index, sum_current_edge_part, current_finished_moat_sum,
							current_cluster_index);
				}
				sum_other_edge_part = 0.0d;
				other_cluster_index = -1;
				other_finished_moat_sum = -1d;
				get_sum_on_edge_part(other_edge_part_index, sum_other_edge_part, other_finished_moat_sum,
						other_cluster_index, 2);
				if (verbosity_level >= 3) {
					System.out.format(
							"other_edge_part_index : %d ; sum_other_edge_part : %6f ; other_finished_moat_sum : %6f ; other_cluster_index : %d\n",
							other_edge_part_index, sum_other_edge_part, other_finished_moat_sum, other_cluster_index);
				}
				double remainder = current_edge_cost - sum_current_edge_part - sum_other_edge_part;
				current_cluster = clusters.get(current_cluster_index);
				other_cluster = clusters.get(other_cluster_index);
				next_edge_part = edge_parts.get(next_edge_part_index);
				other_edge_part = edge_parts.get(other_edge_part_index);
				if (verbosity_level >= 2) {
					System.out.format(
							"Edge event at time %6f, current edge part %d (cluster %d), other edge part %d (cluster %d)\n",
							current_time, next_edge_part_index, current_cluster_index, other_edge_part_index,
							other_cluster_index);
					System.out.format("Sum current part %6f, other part %6f, total length %6f, remainder %6f\n",
							sum_current_edge_part, sum_other_edge_part, current_edge_cost, remainder);
				}
				if (verbosity_level >= 3) {
					System.out.format("current_cluster_index : %d ; other_cluster_index : %d\n", current_cluster_index,
							other_cluster_index);
				}
				if (current_cluster_index == other_cluster_index) {
					stats.num_merged_edge_events += 1;
					if (verbosity_level >= 2) {
						System.out.println("Clusters already merged, ignoring edge\n");
					}
					edge_parts.get(other_edge_part_index).deleted = true;
					continue;
				}
				// merge two clusters
				if (remainder < eps * current_edge_cost) {
					stats.total_num_merge_events += 1;
					phase1_result.add(next_edge_part_index / 2);
					edge_parts.get(other_edge_part_index).deleted = true;
					int new_cluster_index = clusters.size();
					clusters.add(new Cluster(pairing_heap_buffer));
					Cluster new_cluster = clusters.get(new_cluster_index);
					current_cluster = clusters.get(current_cluster_index);
					other_cluster = clusters.get(other_cluster_index);
					if (verbosity_level >= 2) {
						System.out.format("Merge %d and %d into %d\n", current_cluster_index, other_cluster_index,
								new_cluster_index);
					}
					new_cluster.moat = 0.0;
					new_cluster.prize_sum = current_cluster.prize_sum + other_cluster.prize_sum;
					new_cluster.subcluster_moat_sum = current_cluster.subcluster_moat_sum
							+ other_cluster.subcluster_moat_sum;
					new_cluster.contains_root = current_cluster.contains_root || other_cluster.contains_root;
					new_cluster.active = !new_cluster.contains_root;
					new_cluster.merged_along = next_edge_part_index / 2;
					new_cluster.child_cluster_1 = current_cluster_index;
					new_cluster.child_cluster_2 = other_cluster_index;
					new_cluster.necessary = false;
					new_cluster.skip_up = -1;
					new_cluster.skip_up_sum = 0.0;
					new_cluster.merged_into = -1;
					current_cluster.active = false;
					current_cluster.active_end_time = current_time + remainder;
					current_cluster.merged_into = new_cluster_index;
					current_cluster.moat = current_cluster.active_end_time - current_cluster.active_start_time;
					clusters_deactivation.deleteElement(current_cluster_index);
					num_active_clusters -= 1;
					if (!current_cluster.edge_parts.is_empty()) {
						clusters_next_edge_event.deleteElement(current_cluster_index);
					}
					// merge with active or inactive cluster
					if (other_cluster.active) {
						stats.num_active_active_merge_events += 1;
						other_cluster.active = false;
						other_cluster.active_end_time = current_time + remainder;
						other_cluster.moat = other_cluster.active_end_time - other_cluster.active_start_time;
						clusters_deactivation.deleteElement(other_cluster_index);
						if (!other_cluster.edge_parts.is_empty()) {
							clusters_next_edge_event.deleteElement(other_cluster_index);
						}
						num_active_clusters -= 1;
					} else {
						stats.num_active_inactive_merge_events += 1;
						if (!other_cluster.contains_root) {
							double edge_event_update_time = current_time + remainder - other_cluster.active_end_time;
							other_cluster.edge_parts.addToHeap(edge_event_update_time);
							inactive_merge_events.add(new InactiveMergeEvent());
							InactiveMergeEvent merge_event = inactive_merge_events
									.get(inactive_merge_events.size() - 1);
							merge_event.active_cluster_index = current_cluster_index;
							merge_event.inactive_cluster_index = other_cluster_index;
							int active_node_part = edges.get(next_edge_part_index / 2)[0];
							int inactive_node_part = edges.get(next_edge_part_index / 2)[1];
							if ((next_edge_part_index % 2) == 1) {
								int tmp = active_node_part;
								active_node_part = inactive_node_part;
								inactive_node_part = tmp;
							}
							merge_event.active_cluster_node = active_node_part;
							merge_event.inactive_cluster_node = inactive_node_part;
							edge_info.get(next_edge_part_index / 2).inactiveMergeEvent = inactive_merge_events.size()
									- 1;
						}
					}
					other_cluster.merged_into = new_cluster_index;
					new_cluster.edge_parts = current_cluster.edge_parts.meld(current_cluster.edge_parts,
							other_cluster.edge_parts);
					new_cluster.subcluster_moat_sum += current_cluster.moat;
					new_cluster.subcluster_moat_sum += other_cluster.moat;
					if (new_cluster.active) {// new_cluster is inactive if new
												// cluster contains root
						new_cluster.active_start_time = current_time + remainder;
						double becoming_inactive_time = current_time + remainder + new_cluster.prize_sum
								- new_cluster.subcluster_moat_sum;
						clusters_deactivation.insert(becoming_inactive_time, new_cluster_index);
						if (!new_cluster.edge_parts.is_empty()) {
							double tmp_val = 0.0d;
							int tmp_index = -1;
							new_cluster.edge_parts.get_min(tmp_val, tmp_index);
							tmp_val = new_cluster.edge_parts.getMinFirstP;
							tmp_index = new_cluster.edge_parts.getMinSecondP;
							clusters_next_edge_event.insert(tmp_val, new_cluster_index);
						}
						num_active_clusters += 1;
					}
					// do not merge two clusters, but the edge still
					// tight.(since the other edge part is not tight)
				} else if (other_cluster.active) {
					stats.total_num_edge_growth_events += 1;
					stats.num_active_active_edge_growth_events += 1;
					double next_event_time = current_time + remainder / 2.0;
					next_edge_part.nextEventVal = sum_current_edge_part + remainder / 2.0;
					if (!current_cluster.edge_parts.is_empty()) {
						clusters_next_edge_event.deleteElement(current_cluster_index);
					}
					next_edge_part.heapNode = current_cluster.edge_parts.insert(next_event_time, next_edge_part_index);
					double tmp_val = -1.0;
					int tmp_index = -1;
					current_cluster.edge_parts.get_min(tmp_val, tmp_index);
					tmp_val = current_cluster.edge_parts.getMinFirstP;
					tmp_index = current_cluster.edge_parts.getMinSecondP;
					clusters_next_edge_event.insert(tmp_val, current_cluster_index);
					clusters_next_edge_event.deleteElement(other_cluster_index);
					other_cluster.edge_parts.decreaseKey(other_edge_part.heapNode,
							other_cluster.active_start_time + other_edge_part.nextEventVal - other_finished_moat_sum,
							next_event_time);
					other_cluster.edge_parts.get_min(tmp_val, tmp_index);
					tmp_val = other_cluster.edge_parts.getMinFirstP;
					tmp_index = other_cluster.edge_parts.getMinSecondP;
					clusters_next_edge_event.insert(tmp_val, other_cluster_index);
					other_edge_part.nextEventVal = sum_other_edge_part + remainder / 2.0;
					if (verbosity_level >= 2) {
						System.out.format("Added new event at time %6f\n", next_event_time);
					}
					// generate new edge events
				} else {
					stats.total_num_edge_growth_events += 1;
					stats.num_active_inactive_edge_growth_events += 1;
					double next_event_time = current_time + remainder;
					next_edge_part.nextEventVal = current_edge_cost - other_finished_moat_sum;
					if (!current_cluster.edge_parts.is_empty()) {
						clusters_next_edge_event.deleteElement(current_cluster_index);
					}
					next_edge_part.heapNode = current_cluster.edge_parts.insert(next_event_time, next_edge_part_index);
					double tmp_val = -1.0;
					int tmp_index = -1;
					current_cluster.edge_parts.get_min(tmp_val, tmp_index);
					tmp_val = current_cluster.edge_parts.getMinFirstP;
					tmp_index = current_cluster.edge_parts.getMinSecondP;
					clusters_next_edge_event.insert(tmp_val, current_cluster_index);
					other_cluster.edge_parts.decreaseKey(other_edge_part.heapNode,
							other_cluster.active_end_time + other_edge_part.nextEventVal - other_finished_moat_sum,
							other_cluster.active_end_time);
					other_edge_part.nextEventVal = other_finished_moat_sum;
					if (verbosity_level >= 2) {
						System.out.format("Added new event at time %6f and event for inactive edge part\n",
								next_event_time);
					}
				}
				// cluster is tight
			} else {
				stats.num_cluster_events += 1;
				current_time = next_cluster_time;
				remove_next_cluster_event();
				Cluster cur_cluster = clusters.get(next_cluster_index);
				cur_cluster.active = false;
				cur_cluster.active_end_time = current_time;
				cur_cluster.moat = cur_cluster.active_end_time - cur_cluster.active_start_time;
				if (!cur_cluster.edge_parts.is_empty()) {
					clusters_next_edge_event.deleteElement(next_cluster_index);
				}
				num_active_clusters -= 1;
				if (verbosity_level >= 2) {
					System.out.format("Cluster deactivation: cluster %d at time %6f (moat size %6f)\n",
							next_cluster_index, current_time, cur_cluster.moat);
				}
			}
		} // while(num_active_clusters > target_num_active_clusters)

		if (verbosity_level >= 1) {
			System.out.format("Finished GW clustering: final event time %6f, number of edge events %d\n", current_time,
					stats.total_num_edge_events);
		}
		node_good = new ArrayList<Boolean>();
		for (int i = 0; i < prizes.size(); i++) {
			node_good.add(false);
		}
		if (root >= 0) {
			System.out.println("has not checked");
			System.exit(0);
			// find the root cluster
			for (int ii = 0; ii < clusters.size(); ++ii) {
				if (clusters.get(ii).contains_root && clusters.get(ii).merged_into == -1) {
					mark_nodes_as_good(ii);
					break;
				}
			}
		} else {
			for (int ii = 0; ii < clusters.size(); ++ii) {
				if (clusters.get(ii).active) {
					mark_nodes_as_good(ii);
				}
			}
		}

		// if there is no pruning needed, just return the phase 1's result
		if (pruning == PruningMethod.kNoPruning) {
			build_phase1_node_set(phase1_result, result_nodes);
			phase1_result = this.build_phase1_node_set_first_P;
			result_nodes = this.build_phase1_node_set_second_P;
			result_edges = phase1_result;
			return true;
		}
		if (verbosity_level >= 2) {
			System.out.format("------------------------------------------\n");
			System.out.format("Starting pruning\n");
		}
		for (int ii = 0; ii < phase1_result.size(); ++ii) {
			int endPoint0 = edges.get(phase1_result.get(ii))[0];
			int endPoint1 = edges.get(phase1_result.get(ii))[1];
			if (node_good.get(endPoint0) && node_good.get(endPoint1)) {
				phase2_result.add(phase1_result.get(ii));
			}
		}
		if (pruning == PruningMethod.kSimplePruning) {
			build_phase2_node_set(result_nodes);
			result_nodes = this.build_phase2_node_set_first_P;
			result_edges = phase2_result;
			return true;
		}
		ArrayList<Integer> phase3_result = new ArrayList<Integer>();
		phase3_neighbors = Utils.resize(phase3_neighbors, prizes.size(), new ArrayList<Pair<Integer, Double>>());
		for (int ii = 0; ii < phase2_result.size(); ++ii) {
			int cur_edge_index = phase2_result.get(ii);
			int uu = edges.get(cur_edge_index)[0];
			int vv = edges.get(cur_edge_index)[1];
			double cur_cost = costs.get(cur_edge_index);
			phase3_neighbors.get(uu).add(new Pair<Integer, Double>(vv, cur_cost));
			phase3_neighbors.get(vv).add(new Pair<Integer, Double>(uu, cur_cost));
		}

		// GW pruning (this is not strong pruning)
		if (pruning == PruningMethod.kGWPruning) {
			if (verbosity_level >= 2) {
				System.out.format("Starting GW pruning, phase 2 result:\n");
				for (int ii = 0; ii < phase2_result.size(); ++ii) {
					System.out.format("%d ", phase2_result.get(ii));
				}
				System.out.println("\n");
			}
			for (int ii = phase2_result.size() - 1; ii >= 0; --ii) {
				int cur_edge_index = phase2_result.get(ii);
				int uu = edges.get(cur_edge_index)[0];
				int vv = edges.get(cur_edge_index)[1];
				if (node_deleted.get(uu) && node_deleted.get(vv)) {
					if (verbosity_level >= 2) {
						System.out.format("Not keeping edge %d (%d, %d) because both endpoints already deleted\n",
								cur_edge_index, uu, vv);
					}
					continue;
				}
				if (edge_info.get(cur_edge_index).inactiveMergeEvent < 0) {
					mark_clusters_as_necessary(uu);
					mark_clusters_as_necessary(vv);
					phase3_result.add(cur_edge_index);
					if (verbosity_level >= 2) {
						System.out.format("Both endpoint clusters were active, so keeping edge %d (%d, %d)\n",
								cur_edge_index, uu, vv);
					}
				} else {
					InactiveMergeEvent cur_merge_event = inactive_merge_events
							.get(edge_info.get(cur_edge_index).inactiveMergeEvent);
					int active_side_node = cur_merge_event.active_cluster_node;
					int inactive_side_node = cur_merge_event.inactive_cluster_node;
					int inactive_cluster_index = cur_merge_event.inactive_cluster_index;
					if (clusters.get(inactive_cluster_index).necessary) {
						phase3_result.add(cur_edge_index);
						mark_clusters_as_necessary(inactive_side_node);
						mark_clusters_as_necessary(active_side_node);
						if (verbosity_level >= 2) {
							System.out.format(
									"One endpoint was inactive but is marked necessary (%d), so keeping edge %d (%d, %d)\n",
									inactive_cluster_index, cur_edge_index, uu, vv);
						}
					} else {
						mark_nodes_as_deleted(inactive_side_node, active_side_node);
						if (verbosity_level >= 2) {
							System.out.format(
									"One endpoint was inactive and not marked necessary (%d), so discarding edge %d (%d, %d)\n",
									inactive_cluster_index, cur_edge_index, uu, vv);
						}
					}
				}
			}
			build_phase3_node_set(result_nodes);
			result_nodes = this.build_phase3_node_set_first_P;
			result_edges = phase3_result;
			return true;
			// strong pruning method
		} else if (pruning == PruningMethod.kStrongPruning) {
			if (verbosity_level >= 2) {
				System.out.format("Starting Strong pruning, phase 2 result:\n");
				for (int ii = 0; ii < phase2_result.size(); ++ii) {
					System.out.format("%d ", phase2_result.get(ii));
				}
				System.out.println("\n");
			}
			final_component_label = Utils.resize(final_component_label, prizes.size(), -1);
			root_component_index = -1;
			strong_pruning_parent = Utils.resize(strong_pruning_parent, prizes.size(),
					new Pair<Integer, Double>(-1, -1.0d));
			strong_pruning_payoff = Utils.resize(strong_pruning_payoff, prizes.size(), -1.0);
			for (int ii = 0; ii < phase2_result.size(); ++ii) {
				int cur_node_index = edges.get(phase2_result.get(ii))[0];
				if (final_component_label.get(cur_node_index) == -1) {
					final_components.add(new ArrayList<Integer>());
					label_final_component(cur_node_index, final_components.size() - 1);
				}
			}
			if (verbosity_level >= 3) {
				System.out.format("number of final_components : %d\n", final_components.size());
			}
			for (int ii = 0; ii < (int) final_components.size(); ++ii) {
				if (verbosity_level >= 2) {
					System.out.format("Strong pruning on final component %d (size %d):\n", ii,
							final_components.get(ii).size());
				}
				if (ii == root_component_index) {
					if (verbosity_level >= 2) {
						System.out.format("Component contains root, pruning starting at %d\n", root);
					}
					strong_pruning_from(root, true);
				} else {
					int best_component_root = find_best_component_root(ii);
					if (verbosity_level >= 3) {
						System.out.format("best_component_root : %d\n", best_component_root);
					}
					if (verbosity_level >= 2) {
						System.out.println("Best start node for current component: " + best_component_root
								+ ", pruning from there\n");
					}
					strong_pruning_from(best_component_root, true);
				}
			}

			for (int ii = 0; ii < phase2_result.size(); ++ii) {
				int cur_edge_index = phase2_result.get(ii);
				int uu = edges.get(cur_edge_index)[0];
				int vv = edges.get(cur_edge_index)[1];
				if (node_deleted.get(uu) || node_deleted.get(vv)) {
					if (verbosity_level >= 2) {
						System.out.println("Not keeping edge " + cur_edge_index + " (" + uu + ", " + vv
								+ ") because at least one endpoint already deleted\n");
					}
				} else {
					phase3_result.add(cur_edge_index);
				}
			}
			build_phase3_node_set(result_nodes);
			result_nodes = this.build_phase3_node_set_first_P;
			result_edges = phase3_result;
			this.result_edges = result_edges;
			this.result_nodes = result_nodes;
			return true;
		}
		System.out.println("Error: unknown pruning scheme.\n");
		return false;
	}// run

	public void label_final_component(int start_node_index, int new_component_index) {
		cluster_queue.clear();
		cluster_queue.add(start_node_index);
		final_component_label.set(start_node_index, new_component_index);
		int queue_next = 0;
		while (queue_next < cluster_queue.size()) {
			int cur_node_index = cluster_queue.get(queue_next);
			queue_next += 1;
			final_components.get(new_component_index).add(cur_node_index);
			if (cur_node_index == root) {
				root_component_index = new_component_index;
			}
			for (int ii = 0; ii < phase3_neighbors.get(cur_node_index).size(); ++ii) {
				int next_node_index = phase3_neighbors.get(cur_node_index).get(ii).getFirst();
				if (final_component_label.get(next_node_index) == -1) {
					cluster_queue.add(next_node_index);
					final_component_label.set(next_node_index, new_component_index);
				}
			}
		}
	}

	public void strong_pruning_from(int start_node_index, boolean mark_as_deleted) {
		stack.clear();
		stack.add(new Pair<Boolean, Integer>(true, start_node_index));
		strong_pruning_parent.set(start_node_index, new Pair<Integer, Double>(-1, 0.0));
		if (verbosity_level >= 3) {
			System.out.format("phase3_neighbors size : %d\n", phase3_neighbors.size());
		}
		if (verbosity_level >= 3) {
			System.out.format("stack size is : %d\n", stack.size());
		}
		while (!stack.isEmpty()) {
			int lastElementIndex = stack.size() - 1;
			boolean begin = stack.get(lastElementIndex).getFirst();
			int cur_node_index = stack.get(lastElementIndex).getSecond();
			stack.remove(lastElementIndex);
			if (begin) {
				stack.add(new Pair<Boolean, Integer>(false, cur_node_index));
				for (int ii = 0; ii < phase3_neighbors.get(cur_node_index).size(); ++ii) {
					int next_node_index = phase3_neighbors.get(cur_node_index).get(ii).getFirst();
					double next_cost = phase3_neighbors.get(cur_node_index).get(ii).getSecond();
					if (next_node_index == strong_pruning_parent.get(cur_node_index).getFirst()) {
						continue;
					}
					strong_pruning_parent.set(next_node_index, new Pair<Integer, Double>(cur_node_index, next_cost));
					stack.add(new Pair<Boolean, Integer>(true, next_node_index));
				}
			} else {
				strong_pruning_payoff.set(cur_node_index, prizes.get(cur_node_index));
				for (int ii = 0; ii < phase3_neighbors.get(cur_node_index).size(); ++ii) {
					int next_node_index = phase3_neighbors.get(cur_node_index).get(ii).getFirst();
					double next_cost = phase3_neighbors.get(cur_node_index).get(ii).getSecond();
					if (next_node_index == strong_pruning_parent.get(cur_node_index).getFirst().intValue()) {
						continue;
					}
					double next_payoff = strong_pruning_payoff.get(next_node_index) - next_cost;
					if (next_payoff <= 0.0) {
						if (mark_as_deleted) {
							if (verbosity_level >= 2) {
								System.out.format(
										"Subtree starting at %d has a nonpositive contribution of %6f, pruning (good side: %d)\n",
										next_node_index, next_payoff, cur_node_index);
							}
							mark_nodes_as_deleted(next_node_index, cur_node_index);
						}
					} else {
						double currentValue = strong_pruning_payoff.get(cur_node_index);
						strong_pruning_payoff.set(cur_node_index, currentValue + next_payoff);
					}
				}
			}
		}
	}

	public int find_best_component_root(int component_index) {
		int cur_best_root_index = final_components.get(component_index).get(0);
		strong_pruning_from(cur_best_root_index, false);
		double cur_best_value = strong_pruning_payoff.get(cur_best_root_index);
		stack2.clear();
		for (int ii = 0; ii < phase3_neighbors.get(cur_best_root_index).size(); ++ii) {
			stack2.add(phase3_neighbors.get(cur_best_root_index).get(ii).getFirst());
		}
		while (!stack2.isEmpty()) {
			int cur_node_index = stack2.get(stack2.size() - 1);
			stack2.remove(stack2.size() - 1);
			int cur_parent_index = strong_pruning_parent.get(cur_node_index).getFirst();
			double parent_edge_cost = strong_pruning_parent.get(cur_node_index).getSecond();
			double parent_val_without_cur_node = strong_pruning_payoff.get(cur_parent_index);
			double cur_node_net_payoff = strong_pruning_payoff.get(cur_node_index) - parent_edge_cost;
			if (cur_node_net_payoff > 0.0) {
				parent_val_without_cur_node -= cur_node_net_payoff;
			}
			if (parent_val_without_cur_node > parent_edge_cost) {
				double currentPayOff = strong_pruning_payoff.get(cur_node_index);
				strong_pruning_payoff.set(cur_node_index,
						currentPayOff + (parent_val_without_cur_node - parent_edge_cost));
			}
			if (strong_pruning_payoff.get(cur_node_index) > cur_best_value) {
				cur_best_root_index = cur_node_index;
				cur_best_value = strong_pruning_payoff.get(cur_node_index);
			}
			for (int ii = 0; ii < phase3_neighbors.get(cur_node_index).size(); ++ii) {
				int next_node_index = phase3_neighbors.get(cur_node_index).get(ii).getFirst();
				if (next_node_index != cur_parent_index) {
					stack2.add(next_node_index);
				}
			}
		}
		return cur_best_root_index;
	}

	public void build_phase3_node_set(ArrayList<Integer> node_set) {
		node_set.clear();
		for (int ii = 0; ii < (int) prizes.size(); ++ii) {
			if (!node_deleted.get(ii) && node_good.get(ii)) {
				node_set.add(ii);
			}
		}
		this.build_phase3_node_set_first_P = node_set;
	}

	public void build_phase2_node_set(ArrayList<Integer> node_set) {
		node_set.clear();
		for (int ii = 0; ii < (int) prizes.size(); ++ii) {
			if (node_good.get(ii)) {
				node_set.add(ii);
			}
		}
		this.build_phase2_node_set_first_P = node_set;
	}

	public void build_phase1_node_set(ArrayList<Integer> edge_set, ArrayList<Integer> node_set) {
		ArrayList<Boolean> included = new ArrayList<Boolean>();
		for (int i = 0; i < prizes.size(); i++) {
			included.add(false);
		}
		node_set.clear();
		for (int ii = 0; ii < edge_set.size(); ++ii) {
			int uu = edges.get(edge_set.get(ii))[0];
			int vv = edges.get(edge_set.get(ii))[1];

			if (!included.get(uu)) {
				included.set(uu, true);
				node_set.add(uu);
			}
			if (!included.get(vv)) {
				included.set(vv, true);
				node_set.add(vv);
			}
		}
		for (int ii = 0; ii < prizes.size(); ++ii) {
			if (node_good.get(ii) && (!included.get(ii))) {
				node_set.add(ii);
			}
		}
		this.build_phase1_node_set_first_P = edge_set;
		this.build_phase1_node_set_second_P = node_set;
	}

	public void get_statistics(FastPCST.Statistics s) {
		s = stats;
	}

	public enum PruningMethod {
		kNoPruning(0), kSimplePruning(1), kGWPruning(2), kStrongPruning(3), kUnknownPruning(4);

		private int intValue;
		private static HashMap<Integer, PruningMethod> mappings;

		private static HashMap<Integer, PruningMethod> getMappings() {
			if (mappings == null) {
				synchronized (PruningMethod.class) {
					if (mappings == null) {
						mappings = new HashMap<Integer, PruningMethod>();
					}
				}
			}
			return mappings;
		}

		private PruningMethod(int value) {
			intValue = value;
			getMappings().put(value, this);
		}

		public int getValue() {
			return intValue;
		}

		public static PruningMethod forValue(int value) {
			return getMappings().get(value);
		}
	}// PruningMethod

	public class Statistics {
		public long total_num_edge_events;
		public long num_deleted_edge_events;
		public long num_merged_edge_events;
		public long total_num_merge_events;
		public long num_active_active_merge_events;
		public long num_active_inactive_merge_events;
		public long total_num_edge_growth_events;
		public long num_active_active_edge_growth_events;
		public long num_active_inactive_edge_growth_events;
		public long num_cluster_events;
	}

	public class InactiveMergeEvent {
		public int active_cluster_index;
		public int inactive_cluster_index;
		public int active_cluster_node;
		public int inactive_cluster_node;
	}

	public class Cluster {
		public PairingHeap edge_parts;
		public boolean active;
		public double active_start_time;
		public double active_end_time;
		public int merged_into;
		public double prize_sum;
		public double subcluster_moat_sum;
		public double moat;
		public boolean contains_root;
		public int skip_up;
		public double skip_up_sum;
		public int merged_along;
		public int child_cluster_1;
		public int child_cluster_2;
		public boolean necessary;

		public Cluster(ArrayList<PairingHeap.Node> heap_buffer) {
			this.edge_parts = new PairingHeap(heap_buffer);
		}
	}
}
