<?php
/**
 * Simple database cleaning script
 * Cleans experiments that lasted only less than 60 minutes or that ones quited 
 * abnormaly, without storing experimentStop times. Special precaution is taken
 * not to delete last actual running experiment.
 * 
 * Experiment is checked for data alive checks as well. If there are data alive checks
 * from one experiment from bigger interval than 60 minutes, experiment is left alone
 * and stop time is corrected
 */ 

// at first please check results of dry run without db cleaning before delete
$dryrun = true;
// number of seconds of experiment duration to consider experiment to delete
$timeout = 60*60;

$conn = mysql_connect('127.0.0.1', 'xklinec', 'xyhCmFFTQqfmm7QP', true) || die('Cannot connect to database');
mysql_select_db('xklinec') || die('Cannot select database');

// set long timeout
mysql_query('SET WAIT_TIMEOUT=' . 60*60*3);

// query selects old and useless database experiment records
$sql = 'SELECT TIME_TO_SEC(datestop-datestart) as duration, e.* 
            FROM `ExperimentMetadata` e 
            WHERE TIME_TO_SEC(datestop-datestart) < '.$timeout.' OR 
            (e.datestop IS NULL AND id NOT IN 
                (SELECT e2.id FROM `ExperimentMetadata` e2 WHERE e2.datestop IS NULL AND 
                    datestart=(SELECT MAX(datestart) FROM `ExperimentMetadata` WHERE datestop IS NULL))
            )
            ORDER BY datestart
            LIMIT 5000';

$res = mysql_query($sql);
if (!$res) die('Something is wrong with select query, please fix it');

$cleaningSQLs = '
DELETE FROM ExperimentCTPDebug WHERE 	experiment_id=/*[VARIABLE]*/;
DELETE FROM ExperimentCTPInfoStatus WHERE 	experiment_id=/*[VARIABLE]*/;
DELETE FROM ExperimentCTPReport WHERE 	experiment_id=/*[VARIABLE]*/;
DELETE FROM ExperimentCTPRequest WHERE 	experiment_id=/*[VARIABLE]*/;

DELETE FROM experimentDataAliveCheck WHERE 	experiment_id=/*[VARIABLE]*/;
DELETE FROM experimentDataCommands WHERE 	experiment_id=/*[VARIABLE]*/;
DELETE FROM experimentDataGenericMessage WHERE 	experiment_id=/*[VARIABLE]*/;
DELETE FROM ExperimentDataLog WHERE 	experiment_id=/*[VARIABLE]*/;
DELETE FROM experimentDataNoise WHERE 	experiment_id=/*[VARIABLE]*/;
DELETE FROM ExperimentDataParameters WHERE 	experiment_id=/*[VARIABLE]*/;
DELETE FROM ExperimentDataRevokedCycles WHERE 	experiment_id=/*[VARIABLE]*/;
DELETE FROM experimentDataRSSI WHERE 	experiment_id=/*[VARIABLE]*/;
DELETE FROM ExperimentMultiPingRequest WHERE 	experiment_id=/*[VARIABLE]*/;

';

$cleaningSQLsArr = explode(';', $cleaningSQLs);

$ids2delete = array();
while($result = mysql_fetch_assoc($res)){
    $result['datestop'] = intval($result['datestop']);
    echo "Found experiment id: {$result['id']} with duration: {$result['duration']} secs \t from {$result['datestart']} to {$result['datestop']}\n";
    
    // consider alive checks duration if stop is empty
    if (true){
        $sql = 'select MAX(miliFromStart) as max, MIN(miliFromStart) as min, count(*) as cnt FROM `experimentDataAliveCheck`  WHERE  `experiment_id`=' . $result['id'];
        $res2 = mysql_query($sql);
        if ($res2===false) die('Problem with range error');
        
        $row = mysql_fetch_array($res2);
        if(empty($row)){
            echo "## Experiment has no alive checks! Please inspect it\n";
        } else {   
            $max = intval($row['max']);
            $min = intval($row['min']);
            
            $range = ($max-$min)/1000;
            if ($range>=$timeout){
                echo "## Found that experiment took more than expected: {$range} s. Max: {$max}. Skipping\n";
                
                // correct stop time
                if (empty($result['datestop'])){
                    $sql = 'UPDATE ExperimentMetadata SET datestop=FROM_UNIXTIME('.ceil($row['max']/1000).') WHERE id=' . $result['id'];
                    echo "SQL: $sql\n";
                    mysql_query($sql);
                }
                
                continue;
            } else {
                echo "Real duration: $range s\n";
                
                // correct stop time
                if (empty($result['datestop'])){
                    $sql = 'UPDATE ExperimentMetadata SET datestop=FROM_UNIXTIME('.($row['max']/1000).') WHERE id=' . $result['id'];
                    echo "SQL: $sql\n";
                    mysql_query($sql);
                }
            }
        }
    }
    
    // foreach cleaning SQL
    foreach($cleaningSQLsArr as $clSql){
        $sql = trim($clSql);
        if (empty($sql)) continue;
        
        $sql = str_replace('/*[VARIABLE]*/', $result['id'], $sql);
        
        // idempotent operations
        echo "\tGoind to execute[".date('H:m:s')."]: $sql \n";
        if ($dryrun===false){
            $tmp = mysql_query($sql);
            if($tmp===false){
                echo "Problem with query, cannot continue\n";
                die();
            }
        }
    }

    $ids2delete[] = $result['id'];
}

// everything went ok?
foreach($ids2delete as $id){
    echo "Finaly removing experiment with id: {$id} \n";
    if ($dryrun===false){
        mysql_query('DELETE FROM ExperimentMetadata WHERE 	id=' . $id);
        mysql_query('DELETE FROM ExperimentMetadata_connectedNodesUsed  WHERE 	ExperimentMetadata_id=' . $id);
    }
}
