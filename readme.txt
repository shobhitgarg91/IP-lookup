Steps for running the code: 

1. Run the following command to compile code: 

javac IPLookup.java Node.java Node2.java dataClass.java

2. run the code and pass the the name of buildforwardingtable file and lookup file as command line arguments.

For example: java IPLookup build_forwarding_table.txt lookups_ip.txt

3. the code will execute printing the time taken for 100 runs of naive implementation and Trie implementation. 
   It will also print the number of not found entries. If an entry is not found, it takes the first entry present in the buildforwarding table as the default entry.

4. The results are stored in the files Results_naive.txt and Results_trie.txt

5. To compare the two result files, use the diff command:

For example: 	diff Results_naive.txt Results_trie.txt

6. If the entries in both the files are identical, diff will not output anything. 