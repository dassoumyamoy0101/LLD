import java.util.*;

public class LLD_FileSystem {
    public static void main(String[] args) {
        FileSystemNode root = new FolderNode("", "", null), presentNode = root;
        LinkedList<FolderNode> stackFromRoot = new LinkedList<>();
        stackFromRoot.add((FolderNode)root);

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the commands: (End END to end)");

        while (true) { 
            String cmd = sc.next();
            if(cmd.equals("END")) {
                break;
            }
            else if(cmd.equals("mkdir")) {
                String path = sc.next();
                boolean ret = ((FolderNode) presentNode).mkdir(path);
                System.out.println(ret);
            }
            else if(cmd.equals("cd")) {
                String path = sc.next();
                FolderNode ret = ((FolderNode) presentNode).cd(path, stackFromRoot);
                if(ret == null) {
                    System.out.println("Invalid path");
                }
                else {
                    System.out.println("Changed path successfully");
                    presentNode = ret;
                }
            }
            else if(cmd.equals("pwd")) {
                System.out.println(((FolderNode) presentNode).pwd());
            }
            else if(cmd.equals("ls")) {
                List<List<String>> resList = ((FolderNode) presentNode).ls();
                for(List<String> list:resList) {
                    System.out.println(list.get(0) + "\t\t\t" + list.get(1));
                }
            }
            else if(cmd.equals("expand")) {
                List<List<String>> resList = ((FolderNode) presentNode).expand();
                for(List<String> list:resList) {
                    System.out.println(list.get(0) + "\t\t\t\t" + list.get(1));
                }
            }
            else if(cmd.equals("find")) {
                String findString = sc.next();
                List<String> resList = ((FolderNode) presentNode).find(findString);
                for(String s:resList) {
                    System.out.println(s);
                }
            }
            else if(cmd.equals("rm")) {
                String path = sc.next();
                boolean ret = ((FolderNode) presentNode).rm(path);
                if(ret) {
                    System.out.println("Structure deleted successfully");
                }
                else {
                    System.out.println("Invalid path");
                }
            }

            sc.nextLine();
        }
    }    
}

class FolderNode extends FileSystemNode {
    HashMap<String, FileSystemNode> nextNodesMap;

    public FolderNode(String name, String path, FileSystemNode parent) {
        super(name, path, parent);
        this.type = Type.FOLDER;
        this.nextNodesMap = new HashMap<String, FileSystemNode>();
    }

    public boolean touch(String path) {
        StringTokenizer st = new StringTokenizer(path, "/");
        LinkedList<String> names = new LinkedList<String>();
        while(st.hasMoreTokens()) {
            names.add(st.nextToken());
        }
        String fileName = names.removeLast();
        FolderNode node = this;
        while(!names.isEmpty()) {  
            String name = names.removeFirst();
            if(!node.nextNodesMap.containsKey(name)) {
                return false;
            }
            node = (FolderNode)(node.nextNodesMap.get(name));
        }
        if(node.nextNodesMap.containsKey(fileName)) {
            FileSystemNode file = new FileNode(fileName, node.fullPath + "/" + fileName, this);
            node.nextNodesMap.put(fileName, file);
        }
        return false;
    }

    public synchronized boolean rm(String path) {
        FolderNode node = this;
        FolderNode prev = node;
        StringTokenizer st = new StringTokenizer(path, "/");
        String lastName = null;
        while(st.hasMoreTokens() && node.type == Type.FOLDER) {
            String name = st.nextToken();
            if(!node.nextNodesMap.containsKey(name)) {
                return false;
            }
            prev = node;
            lastName = name;
            if(node.nextNodesMap.get(name).type == Type.FOLDER) {
                // if the nxt node is folder
                node = (FolderNode)(node).nextNodesMap.get(name);
            }
            else {
                // if the next node is file
                break;
            }
        }
        if(lastName != null && !st.hasMoreTokens()) {
            prev.nextNodesMap.remove(lastName);
        }
        return true;
    }

    public List<String> find(String findPath) {
        findPath = this.fullPath + findPath;
        List<String> expandedPathsList = new ArrayList<>(), resList = new ArrayList<>();
        dfsTraverseExpandAndStorePaths(this, expandedPathsList);
        for(String path:expandedPathsList) {
            if(regexMatching(path, findPath, path.length(), findPath.length(), 0, 0)) {
                resList.add(path);
            }
        }
        return resList;
    }

    private void dfsTraverseExpandAndStorePaths(FileSystemNode node, List<String> resList) {
        resList.add(node.fullPath);
        if(node.type == Type.FOLDER) {
            FolderNode folderNode = (FolderNode)node;
            for(Map.Entry<String, FileSystemNode> m:folderNode.nextNodesMap.entrySet()) {
                dfsTraverseExpandAndStorePaths(m.getValue(), resList);
            }
        }
    }

    // s2 is the regex pattern
    private boolean regexMatching(String s1, String s2, int n1, int n2, int idx1, int idx2) {
        if(idx1 == n1) {
            if(idx2 == n2) {
                return true;
            }
            return false;
        }
        if(idx2 == n2) {
            return false;
        }
        char ch1 = s1.charAt(idx1), ch2 = s2.charAt(idx2);
        if(ch2 == '*') {
            return regexMatching(s1, s2, n1, n2, idx1+1, idx2) || regexMatching(s1, s2, n1, n2, idx1, idx2+1) || regexMatching(s1, s2, n1, n2, idx1+1, idx2+1);
        }
        if(ch1 == ch2) {
            return regexMatching(s1, s2, n1, n2, idx1+1, idx2+1);
        }
        return false;
    }

    public List<List<String>> expand() {
        List<List<String>> resList = new ArrayList<>();
        dfsTraverse(this, resList, 0);
        return resList;
    }

    private void dfsTraverse(FileSystemNode node, List<List<String>> resList, int depth) {
        String name = "";
        for(int i=0; i<depth; ++i) name += '\t';
        name += node.name;
        resList.add(Arrays.asList(name, node.type.toString()));
        if(node.type == Type.FOLDER) {
            FolderNode folderNode = (FolderNode)node;
            for(Map.Entry<String, FileSystemNode> m:folderNode.nextNodesMap.entrySet()) {
                dfsTraverse(m.getValue(), resList, depth+1);
            }
        }
    }

    public List<List<String>> ls() {
        List<List<String>> resList = new ArrayList<>(nextNodesMap.size());
        for(Map.Entry<String, FileSystemNode> m:nextNodesMap.entrySet()) {
            FileSystemNode fileSystemNode = m.getValue();
            List<String> list = new ArrayList<>(Arrays.asList(fileSystemNode.name, fileSystemNode.type.toString()));
            resList.add(list);
        }
        return resList;
    }

    public String pwd() {
        return this.fullPath;
    }

    public FolderNode cd(String path, LinkedList<FolderNode> stackFromRoot) {
        FolderNode node = this;
        LinkedList<FolderNode> tempStack = new LinkedList<>();
        StringTokenizer st = new StringTokenizer(path, "/");
        while(st.hasMoreTokens()) {
            String name = st.nextToken();

            if(name.equals("..")) {
                if(!tempStack.isEmpty()) {
                    node = tempStack.removeLast();
                }
                else if(!stackFromRoot.isEmpty()) {
                    node = stackFromRoot.removeLast();
                }
                else {
                    return null;
                }
                continue;
            }

            if(!node.nextNodesMap.containsKey(name)) {
                return null;
            }
            node = (FolderNode)(node.nextNodesMap.get(name));
            tempStack.add(node);
        }
        while(!tempStack.isEmpty()) {
            stackFromRoot.add(tempStack.removeFirst());
        }
        return node;
    }

    public synchronized boolean mkdir(String newFolderPath) {
        FolderNode node = this;
        StringTokenizer st = new StringTokenizer(newFolderPath, "/");
        while(st.hasMoreTokens()) {
            String name = st.nextToken();
            if(name.length() == 0 || !checkName(name)) {
                return false;
            }
            // create folder as per the path
            if(!node.nextNodesMap.containsKey(name)) {
                FileSystemNode fileSystemNode = new FolderNode(name, node.fullPath + "/" + name, node);
                node.nextNodesMap.put(name, fileSystemNode);
            }
            node = (FolderNode)((FolderNode) node).nextNodesMap.get(name);
        }

        return true;
    }

    private boolean checkName(String s) {
        if(!Character.isLetter(s.charAt(0))) {
            return false;
        }
        int n = s.length();
        for(int i=0; i<n; ++i) {
            char ch = s.charAt(i);
            if(!(Character.isLetter(ch) || Character.isDigit(ch) || ch == '(' || ch == ')' || ch == '_' || ch == '-')) {
                return false;
            }
        }
        return true;
    }
}

class FileNode extends FileSystemNode {

    public FileNode(String name, String path, FileSystemNode parent) {
        super(name, path, parent);
        this.type = Type.FILE;
    }
}

abstract class FileSystemNode {
    String name, fullPath;
    Type type;
    FileSystemNode parent;          // actually of type FolderNode

    public FileSystemNode(String name, String fullPath, FileSystemNode parent) {
        this.name = name;
        this.fullPath = fullPath;
        this.parent = parent;
    }
}

enum Type {
    FILE, FOLDER
}

/*

Use composite design pattern -- where the design is based on hierarchical structure

Use the concent of n-ary trees (Tries)

The leaf node can either be a file, or an empty directory/folder. 

The file/folder path will contain "/" as delimiter and if the last string contains ".", then the last node will be a file, else folder

Home directory --> /home/userName
Root directory --> /

Commands to service:

ls
pwd
mkdir ...
cd ...
rm ...
find ...   -> handle regex
touch ...
edit <fileName> <text> 


Classes:

abstract FileSystemNode (name, fullPath, List<FileSystemNode> fileSystemNodes, ls()) <--- FileNode (display()), FolderNode (mkdir(), cd(), rm(), find(), touch())



*/