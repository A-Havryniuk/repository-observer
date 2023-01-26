package com.epam.rd.autocode.observer.git;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GitRepoObservers implements Repository {
    private final Map<Branch, List<Commit>> repository;
    private final List<WebHook> webHooks;

    private GitRepoObservers() {
        repository = new HashMap<>();
        webHooks = new ArrayList<>();
        repository.put(new Branch("main"), new ArrayList<>());
    }

    public static Repository newRepository() {
        return new GitRepoObservers();
    }

    public static WebHook mergeToBranchWebHook(String branchName) {
        return new WebHookImpl(branchName, Event.Type.MERGE, new ArrayList<>());
    }

    public static WebHook commitToBranchWebHook(String branchName) {
        return new WebHookImpl(branchName, Event.Type.COMMIT, new ArrayList<>());
    }

    @Override
    public Branch getBranch(String name) {
        return repository.keySet().stream()
                .filter(n -> n.getName().equals(name))
                .findFirst().orElse(null);
    }

    @Override
    public Branch newBranch(Branch sourceBranch, String name) {
        checkExistedBunch(sourceBranch, name);
        Branch emergingBrunch = new Branch(name);
        repository.put(emergingBrunch, new ArrayList<>(repository.get(sourceBranch)));
        return emergingBrunch;
    }

    @Override
    public Commit commit(Branch branch, String author, String[] changes) {
        Commit commit = new Commit(author, changes);
        List<Commit> commits = repository.get(branch);
        commits.add(commit);
        repository.put(branch, commits);
        notifyWebHook(new Event(Event.Type.COMMIT, branch, List.of(commit)));
        return commit;
    }

    @Override
    public void merge(Branch sourceBranch, Branch targetBranch) {
        List<Commit> targetCommits = repository.get(targetBranch);
        List<Commit> diffCommits = repository.get(sourceBranch).stream()
                .filter(n -> !targetCommits.contains(n))
                .collect(Collectors.toList());
        targetCommits.addAll(diffCommits);
        repository.put(targetBranch, targetCommits);
        notifyWebHook(new Event(Event.Type.MERGE, targetBranch, diffCommits));
    }

    @Override
    public void addWebHook(WebHook webHook) {
        webHooks.add(webHook);
    }

    private void checkExistedBunch(Branch branch, String name) {
        if (repository.keySet().stream().anyMatch(n -> n.getName().equals(name)))
            throw new IllegalArgumentException("Branch " +  name + " already exist");
        if (repository.keySet().stream().noneMatch(n -> n.getName().equals(branch.getName())))
            throw new IllegalArgumentException("Branch " +  branch.getName()
                    + " is absent in current repository");
    }

    private void notifyWebHook(Event event) {
        WebHook webHook = webHooks.stream()
                .filter(n -> n.type().equals(event.type())
                        && n.branch().equals(event.branch().getName()))
                .findFirst().orElse(null);
        if (webHook != null && event.commits().size() > 0) {
            webHook.onEvent(event);
        }
    }
}
