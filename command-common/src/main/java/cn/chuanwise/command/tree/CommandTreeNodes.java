package cn.chuanwise.command.tree;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.configuration.CommanderConfiguration;
import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.StaticUtilities;

import java.util.*;

class CommandTreeNodes
    extends StaticUtilities {
    
    // 检查添加子节点，或使用现有节点
    @SuppressWarnings("all")
    public static <T extends CommandTreeNode> List<T> addSon(List<CommandTreeNode> sons, T newSon) {
        Preconditions.namedArgumentNonNull(sons, "sons");

        final CommanderConfiguration configuration = newSon.getCommander().getCommanderConfiguration();

        if (sons.isEmpty()) {
            sons.add(newSon);
            return Collections.singletonList(newSon);
        }

        final Commander commander = newSon.getCommander();
        if (newSon instanceof PlainTextsCommandTreeNode) {
            final PlainTextsCommandTreeNode tree = (PlainTextsCommandTreeNode) newSon;
            final List<String> newTexts = tree.getTexts();

            // 检查是否有交错的部分
            for (CommandTreeNode elderSon : sons) {
                if (!(elderSon instanceof PlainTextsCommandTreeNode)) {
                    continue;
                }

                final PlainTextsCommandTreeNode plainTextsCommandTree = (PlainTextsCommandTreeNode) elderSon;
                final List<String> elderTexts = plainTextsCommandTree.getTexts();

                // 要么完全不相干，要么完全相等
                final List<String> sameParts = new ArrayList<>(elderTexts);
                sameParts.retainAll(newTexts);

                final boolean related = !sameParts.isEmpty();
                if (related) {
                    final boolean equals = sameParts.size() == newTexts.size() && newTexts.size() == elderTexts.size();
                    if (equals) {
                        return Collections.singletonList((T) plainTextsCommandTree);
                    }

                    // 如果相同就合并，否则拆开
                    if (configuration.isMergeRelatedForks()) {
                        final List<String> otherElderParts = new ArrayList<>(elderTexts);
                        otherElderParts.removeAll(sameParts);
                        final boolean newOtherElderCommandTree = !otherElderParts.isEmpty();
                        final PlainTextsCommandTreeNode otherElderPartsCommandTree = newOtherElderCommandTree ? new PlainTextsCommandTreeNode(otherElderParts, commander) : null;

                        final List<String> otherNewParts = new ArrayList<>(newTexts);
                        otherNewParts.removeAll(sameParts);
                        final boolean newOtherNewCommandTree = !otherNewParts.isEmpty();
                        final PlainTextsCommandTreeNode otherNewPartsCommandTree = newOtherNewCommandTree ? new PlainTextsCommandTreeNode(otherNewParts, commander) : null;

                        // 孩子合并后，原来的节点作为公共部分
                        if (newOtherElderCommandTree) {
                            plainTextsCommandTree.texts.clear();
                            plainTextsCommandTree.texts.addAll(sameParts);
                        }

                        // 添加新的孩子
                        for (int i = 0; i < newSon.sons.size(); i++) {
                            final CommandTreeNode grandSon = newSon.sons.get(i);

                            addSon(plainTextsCommandTree.sons, grandSon);

                            if (newOtherNewCommandTree) {
                                addSon(otherNewPartsCommandTree.sons, grandSon);
                            }
                        }
                        if (newOtherElderCommandTree) {
                            for (int i = 0; i < elderSon.sons.size(); i++) {
                                final CommandTreeNode grandSon = elderSon.sons.get(i);
                                addSon(otherElderPartsCommandTree.sons, grandSon);
                            }
                        }

                        if (newOtherElderCommandTree && newOtherNewCommandTree) {
                            return (List) cn.chuanwise.common.util.Collections.asUnmodifiableList(otherNewPartsCommandTree, otherElderPartsCommandTree, plainTextsCommandTree);
                        }

                        // 添加新的树节点
                        if (newOtherElderCommandTree) {
                            addSon(sons, otherElderPartsCommandTree);
                            return (List) cn.chuanwise.common.util.Collections.asUnmodifiableList((CommandTreeNode) plainTextsCommandTree, (CommandTreeNode) otherElderPartsCommandTree);
                        }

                        if (newOtherNewCommandTree) {
                            addSon(sons, otherNewPartsCommandTree);
                            return (List) cn.chuanwise.common.util.Collections.asUnmodifiableList(plainTextsCommandTree, otherNewPartsCommandTree);
                        }
                    } else {
                        // 进行合并操作，其实就是把新孩子并入老孩子
                        for (CommandTreeNode grandSon : elderSon.sons) {
                            addSon(newSon.sons, grandSon);
                        }
                    }
                    return Collections.singletonList((T) plainTextsCommandTree);
                }
            }

            // 如果没有交错部分，添加孩子并返回孩子
            sons.add(newSon);
            return Collections.singletonList(newSon);
        }

        if (newSon instanceof SimpleParameterCommandTreeNode) {
            for (CommandTreeNode son : sons) {
                if (son instanceof SimpleParameterCommandTreeNode) {
                    return Collections.singletonList((T) son);
                }
            }

            sons.add(newSon);
            return Collections.singletonList(newSon);
        }

        if (newSon instanceof NullableOptionalParameterCommandTreeNode) {
            // 检查是否已经有孤儿
            Preconditions.state(sons.isEmpty() || !commander.getCommanderConfiguration().isStrongMatch(), "除非关闭强匹配 strongMatch，否则剩余参数分支不能具备兄弟节点");

            for (CommandTreeNode son : sons) {
                if (son instanceof NullableOptionalParameterCommandTreeNode) {
                    return Collections.singletonList((T) son);
                }
            }

            sons.add(newSon);
            return Collections.singletonList(newSon);
        }

        if (newSon instanceof NonNullOptionalParameterCommandTreeNode) {
            // 检查是否已经有孤儿
            Preconditions.state(sons.isEmpty() || !commander.getCommanderConfiguration().isStrongMatch(), "除非关闭强匹配 strongMatch，否则剩余参数分支不能具备兄弟节点");

            for (CommandTreeNode son : sons) {
                if (son instanceof NonNullOptionalParameterCommandTreeNode) {
                    return Collections.singletonList((T) son);
                }
            }

            sons.add(newSon);
            return Collections.singletonList(newSon);
        }

        if (newSon instanceof OptionCommandTreeNode) {
            sons.add(newSon);
            return Collections.singletonList(newSon);
        }

        throw new NoSuchElementException("未知的孩子节点类型：" + newSon.getClass().getName());
    }
}
