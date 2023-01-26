package com.epam.rd.autocode.observer.git;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GitRepoObserversTest {

    @Test
    public void testCreatingBranches() {
        final Repository repo = GitRepoObservers.newRepository();

        Branch mainBranch = repo.getBranch("main");

        assertNotNull(mainBranch);
        assertNull(repo.getBranch("dev"));
        assertNull(repo.getBranch("release"));

        Branch devBranch = repo.newBranch(mainBranch, "dev");
        assertNotNull(devBranch);
        assertNotNull(repo.getBranch("dev"));
        assertNull(repo.getBranch("release"));

        Branch releaseBranch = repo.newBranch(mainBranch, "release");
        assertNotNull(releaseBranch);
        assertNotNull(repo.getBranch("dev"));
        assertNotNull(repo.getBranch("release"));
    }

    @Test
    public void testExceptionWhenCreatingABranchWithExistingName() {
        final Repository repo = GitRepoObservers.newRepository();

        Branch mainBranch = repo.getBranch("main");
        repo.newBranch(mainBranch, "dev");

        assertThrows(IllegalArgumentException.class, () -> repo.newBranch(mainBranch, "dev"));
    }

    @Test
    public void testExceptionWhenCreatingABranchForkingFromNonExistingSourceBranch() {
        final Repository repo1 = GitRepoObservers.newRepository();
        final Repository repo2 = GitRepoObservers.newRepository();

        Branch main1Branch = repo1.getBranch("main");
        Branch main2Branch = repo2.getBranch("main");
        Branch dev1 = repo1.newBranch(main1Branch, "dev1");
        Branch dev2 = repo2.newBranch(main2Branch, "dev2");

        assertThrows(IllegalArgumentException.class, () -> repo1.newBranch(dev2, "another-branch"));
        assertThrows(IllegalArgumentException.class, () -> repo2.newBranch(dev1, "another-branch"));
    }

    @Test
    public void testForkingBranches() {
        final Repository repo = GitRepoObservers.newRepository();

        Branch mainBranch = repo.getBranch("main");

        repo.commit(mainBranch, "Johnny Mnemonic", new String[]{"Added README.md"});
        repo.commit(mainBranch, "Johnny Mnemonic", new String[]{"Added functional requirements",});

        Branch devBranch = repo.newBranch(mainBranch, "dev");
        WebHook mergeDevWebHook = GitRepoObservers.mergeToBranchWebHook("dev");
        repo.addWebHook(mergeDevWebHook);

        repo.commit(mainBranch, "Johnny Silverhand", new String[]{"Added cyberanarchy manifest",});
        repo.merge(mainBranch, devBranch);

        Branch dev2Branch = repo.newBranch(mainBranch, "dev2");
        WebHook mergeDev2WebHook = GitRepoObservers.mergeToBranchWebHook("dev2");
        repo.addWebHook(mergeDev2WebHook);

        repo.commit(mainBranch, "CrashOverrider", new String[]{"Added full implementation"});
        repo.merge(mainBranch, devBranch);
        repo.merge(devBranch, dev2Branch);

        assertEquals(
                "[Event[MERGE, dev, [Commit[Johnny Silverhand, [Added cyberanarchy manifest]]]]," +
                        " Event[MERGE, dev, [Commit[CrashOverrider, [Added full implementation]]]]]",
                mergeDevWebHook.caughtEvents().toString()
        );
        assertEquals(
                "[Event[MERGE, dev2, " +
                        "[Commit[CrashOverrider, [Added full implementation]]]]]",
                mergeDev2WebHook.caughtEvents().toString()
        );
    }

    @Test
    public void readmeCase() {
        final Repository repo = GitRepoObservers.newRepository();

        Branch mainBranch = repo.getBranch("main");
        Branch devBranch = repo.newBranch(mainBranch, "dev");

        final WebHook commitMainWebHook = GitRepoObservers.commitToBranchWebHook("main");
        final WebHook commitDevWebHook = GitRepoObservers.commitToBranchWebHook("dev");
        final WebHook mergeMainBranch = GitRepoObservers.mergeToBranchWebHook("main");

        repo.addWebHook(mergeMainBranch);
        repo.addWebHook(commitMainWebHook);
        repo.addWebHook(commitDevWebHook);

        repo.commit(
                devBranch,
                "Johnny Mnemonic",
                new String[]{
                        "Added README.md",
                        "Added project description",
                });
        repo.commit(
                devBranch,
                "Johnny Mnemonic",
                new String[]{
                        "Added functional requirements",
                });
        repo.commit(
                devBranch,
                "Johnny Silverhand",
                new String[]{
                        "Added cyberanarchy manifest",
                });

        repo.merge(devBranch, mainBranch);

        assertEquals(
                "[]",
                commitMainWebHook.caughtEvents().toString()
        );
        assertEquals(
                "[Event[COMMIT, dev, [Commit[Johnny Mnemonic, [Added README.md, Added project description]]]]," +
                        " Event[COMMIT, dev, [Commit[Johnny Mnemonic, [Added functional requirements]]]]," +
                        " Event[COMMIT, dev, [Commit[Johnny Silverhand, [Added cyberanarchy manifest]]]]]",
                commitDevWebHook.caughtEvents().toString()
        );
        assertEquals(
                "[Event[MERGE, main, " +
                        "[Commit[Johnny Mnemonic, [Added README.md, Added project description]]," +
                        " Commit[Johnny Mnemonic, [Added functional requirements]]," +
                        " Commit[Johnny Silverhand, [Added cyberanarchy manifest]]]]]",
                mergeMainBranch.caughtEvents().toString()
        );

    }

    @Test
    public void readmeCaseWithPreviousCommits() {
        final Repository repo = GitRepoObservers.newRepository();

        Branch mainBranch = repo.getBranch("main");
        Branch devBranch = repo.newBranch(mainBranch, "dev");

        repo.commit(
                devBranch,
                "Johnny Mnemonic",
                new String[]{"Added README.txt"});
        repo.merge(devBranch, mainBranch);
        repo.commit(
                mainBranch,
                "Johnny Mnemonic",
                new String[]{"Removed README.txt"});
        repo.merge(mainBranch, devBranch);

        final WebHook commitMainWebHook = GitRepoObservers.commitToBranchWebHook("main");
        final WebHook commitDevWebHook = GitRepoObservers.commitToBranchWebHook("dev");
        final WebHook mergeMainBranch = GitRepoObservers.mergeToBranchWebHook("main");

        repo.addWebHook(mergeMainBranch);
        repo.addWebHook(commitMainWebHook);
        repo.addWebHook(commitDevWebHook);

        repo.commit(
                devBranch,
                "Johnny Mnemonic",
                new String[]{
                        "Added README.md",
                        "Added project description",
                });
        repo.commit(
                devBranch,
                "Johnny Mnemonic",
                new String[]{
                        "Added functional requirements",
                });
        repo.commit(
                devBranch,
                "Johnny Silverhand",
                new String[]{
                        "Added cyberanarchy manifest",
                });

        repo.merge(devBranch, mainBranch);

        assertEquals(
                "[]",
                commitMainWebHook.caughtEvents().toString()
        );
        assertEquals(
                "[Event[COMMIT, dev, [Commit[Johnny Mnemonic, [Added README.md, Added project description]]]]," +
                        " Event[COMMIT, dev, [Commit[Johnny Mnemonic, [Added functional requirements]]]]," +
                        " Event[COMMIT, dev, [Commit[Johnny Silverhand, [Added cyberanarchy manifest]]]]]",
                commitDevWebHook.caughtEvents().toString()
        );
        assertEquals(
                "[Event[MERGE, main, " +
                        "[Commit[Johnny Mnemonic, [Added README.md, Added project description]]," +
                        " Commit[Johnny Mnemonic, [Added functional requirements]]," +
                        " Commit[Johnny Silverhand, [Added cyberanarchy manifest]]]]]",
                mergeMainBranch.caughtEvents().toString()
        );

    }


    @Test
    public void readmeCaseWithPreviousCommitsAndBackwardEmptyMerge() {
        final Repository repo = GitRepoObservers.newRepository();
        Branch mainBranch = repo.getBranch("main");
        Branch devBranch = repo.newBranch(mainBranch, "dev");

        repo.commit(
                devBranch,
                "Johnny Mnemonic",
                new String[]{"Added README.txt"});
        repo.merge(devBranch, mainBranch);
        repo.commit(
                mainBranch,
                "Johnny Mnemonic",
                new String[]{"Removed README.txt"});
        repo.merge(mainBranch, devBranch);

        final WebHook commitMainWebHook = GitRepoObservers.commitToBranchWebHook("main");
        final WebHook commitDevWebHook = GitRepoObservers.commitToBranchWebHook("dev");
        final WebHook mergeMainBranch = GitRepoObservers.mergeToBranchWebHook("main");
        final WebHook mergeDevBranch = GitRepoObservers.mergeToBranchWebHook("dev");

        repo.addWebHook(mergeMainBranch);
        repo.addWebHook(commitMainWebHook);
        repo.addWebHook(commitDevWebHook);
        repo.addWebHook(mergeDevBranch);

        repo.commit(
                devBranch,
                "Johnny Mnemonic",
                new String[]{
                        "Added README.md",
                        "Added project description",
                });
        repo.commit(
                devBranch,
                "Johnny Mnemonic",
                new String[]{
                        "Added functional requirements",
                });
        repo.commit(
                devBranch,
                "Johnny Silverhand",
                new String[]{
                        "Added cyberanarchy manifest",
                });

        repo.merge(devBranch, mainBranch);
        repo.merge(mainBranch, devBranch);
        repo.merge(devBranch, mainBranch);


        assertEquals(
                "[]",
                commitMainWebHook.caughtEvents().toString()
        );
        assertEquals(
                "[Event[COMMIT, dev, [Commit[Johnny Mnemonic, [Added README.md, Added project description]]]]," +
                        " Event[COMMIT, dev, [Commit[Johnny Mnemonic, [Added functional requirements]]]]," +
                        " Event[COMMIT, dev, [Commit[Johnny Silverhand, [Added cyberanarchy manifest]]]]]",
                commitDevWebHook.caughtEvents().toString()
        );
        assertEquals(
                "[Event[MERGE, main, " +
                        "[Commit[Johnny Mnemonic, [Added README.md, Added project description]]," +
                        " Commit[Johnny Mnemonic, [Added functional requirements]]," +
                        " Commit[Johnny Silverhand, [Added cyberanarchy manifest]]]]]",
                mergeMainBranch.caughtEvents().toString()
        );
        assertEquals(
                "[]",
                mergeDevBranch.caughtEvents().toString()
        );

    }

    @Test
    public void readmeCaseWithPreviousCommitsAndBackwardNonEmptyMerge() {
        final Repository repo = GitRepoObservers.newRepository();
        Branch mainBranch = repo.getBranch("main");
        Branch devBranch = repo.newBranch(mainBranch, "dev");

        repo.commit(
                devBranch,
                "Johnny Mnemonic",
                new String[]{"Added README.txt"});
        repo.merge(devBranch, mainBranch);
        repo.commit(
                mainBranch,
                "Johnny Mnemonic",
                new String[]{"Removed README.txt"});
        repo.merge(mainBranch, devBranch);

        final WebHook commitMainWebHook = GitRepoObservers.commitToBranchWebHook("main");
        final WebHook commitDevWebHook = GitRepoObservers.commitToBranchWebHook("dev");
        final WebHook mergeMainBranch = GitRepoObservers.mergeToBranchWebHook("main");
        final WebHook mergeDevWebHook = GitRepoObservers.mergeToBranchWebHook("dev");

        repo.addWebHook(commitMainWebHook);
        repo.addWebHook(commitDevWebHook);
        repo.addWebHook(mergeMainBranch);
        repo.addWebHook(mergeDevWebHook);

        repo.commit(
                devBranch,
                "Johnny Mnemonic",
                new String[]{
                        "Added README.md",
                        "Added project description",
                });
        repo.commit(
                devBranch,
                "Johnny Mnemonic",
                new String[]{
                        "Added functional requirements",
                });
        repo.commit(
                devBranch,
                "Johnny Silverhand",
                new String[]{
                        "Added cyberanarchy manifest",
                });

        repo.merge(devBranch, mainBranch);
        repo.commit(mainBranch,
                "CrashOverrider",
                new String[]{"Added full implementation"});

        repo.merge(mainBranch, devBranch);
        repo.merge(devBranch, mainBranch);


        assertEquals(
                "[Event[COMMIT, main, [Commit[CrashOverrider, [Added full implementation]]]]]",
                commitMainWebHook.caughtEvents().toString()
        );
        assertEquals(
                "[Event[COMMIT, dev, [Commit[Johnny Mnemonic, [Added README.md, Added project description]]]]," +
                        " Event[COMMIT, dev, [Commit[Johnny Mnemonic, [Added functional requirements]]]]," +
                        " Event[COMMIT, dev, [Commit[Johnny Silverhand, [Added cyberanarchy manifest]]]]]",
                commitDevWebHook.caughtEvents().toString()
        );
        assertEquals(
                "[Event[MERGE, main, " +
                        "[Commit[Johnny Mnemonic, [Added README.md, Added project description]]," +
                        " Commit[Johnny Mnemonic, [Added functional requirements]]," +
                        " Commit[Johnny Silverhand, [Added cyberanarchy manifest]]]]]",
                mergeMainBranch.caughtEvents().toString()
        );

        assertEquals(
                "[Event[MERGE, dev, [Commit[CrashOverrider, [Added full implementation]]]]]",
                mergeDevWebHook.caughtEvents().toString()
        );

    }
}